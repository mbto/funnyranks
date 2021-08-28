package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.BrokerEvent;
import com.github.mbto.funnyranks.common.dto.FunnyRanksData;
import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.Partition;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.*;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.github.mbto.funnyranks.handlers.MessageHandler;
import com.github.mbto.funnyranks.service.BrokerHolder;
import com.github.mbto.funnyranks.service.GeoLite2UpdaterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.BrokerEvent.*;
import static com.github.mbto.funnyranks.common.Constants.storageByNameContainerSupplier;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;

@Service
@Lazy(false)
@Slf4j
public class Distributor {
    @Autowired
    private BlockingDeque<Message<?>> defaultPartition;
    @Autowired
    private Map<UInteger, MessageHandler> messageHandlerByAppId;
    @Autowired
    private Map<UShort, DatagramSocket> datagramSocketByListenerPort;
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Partition> partitionByPort;
    @Autowired
    private Map<Integer, Partition> partitionById;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private ThreadPoolTaskExecutor receiverTE;
    @Autowired
    private ThreadPoolTaskExecutor consumerTE;

    @Autowired
    private Receiver receiver;
    @Autowired
    private MessagesConsumer messagesConsumer;
    @Autowired
    private FunnyRanksDao funnyRanksDao;
    @Autowired
    private BrokerHolder brokerHolder;
    @Autowired
    private GeoLite2UpdaterService geoLite2UpdaterService;

    @Async("distributorTE")
    public void launchDistributorAsync() {
        log.info("Activating distributor");
        Message<?> message;
        for (; ; ) {
            try {
                message = defaultPartition.takeFirst();
            } catch (Throwable e) {
                log.warn("Exception while takeFirst message", e);
                continue;
            }
            try {
                BrokerEvent brokerEvent = message.getBrokerEvent();
                if (brokerEvent == CONSUME_DATAGRAM) {
                    consumeDatagram(message);
                } else if (brokerEvent == APPLY_CHANGES || brokerEvent == UPDATE_MAXMIND_DB) {
                    consumeSynchronizationEvent(message);
                } else if (brokerEvent == FLUSH_SESSIONS_FROM_FRONTEND || brokerEvent == FLUSH_SESSIONS_FROM_SCHEDULER) {
                    consumeFlush(message);
                } else if (brokerEvent == FLUSH_ALL_SESSIONS_AND_TERMINATE) {
                    log.info("Deactivation distributor detected");
                    consumeFlushAndQuit(message);
                    break;
                } else {
                    log.warn("Unknown brokerEvent " + brokerEvent);
                }
            } catch (Throwable e) {
                log.warn("Exception while handling message " + message, e);
                //noinspection UnnecessaryContinue
                continue;
            }
        }
        log.info("Deactivated distributor");
    }

    private void consumeDatagram(Message<?> originalMessage) {
        UShort portValue = originalMessage.getPort();
        PortData portData = portDataByPort.get(portValue);
        if (portData == null || !portData.isPortActive())
            return;
        Game game = portData.getGame();
        if (game == null)
            return;
        UInteger appId = game.getAppId();
        if (appId == null)
            return;
        MessageHandler messageHandler = messageHandlerByAppId.get(appId);
        if (messageHandler == null)
            return;
        byte[] data = (byte[]) originalMessage.getPojo();
        if (!messageHandler.validate(portValue, data))
            return;
        Message<PortData> message = new Message<>(portValue, messageHandler.convert(portValue, data), portData, null);
        Partition partition = partitionByPort.get(portValue);
        if (log.isDebugEnabled())
            log.debug(portValue + " Sending to " + partition + " message: " + message);
        putLastWithTryes(partition, message);
    }

    private void consumeSynchronizationEvent(Message<?> originalMessage) {
        log.info("Started synchronization by brokerEvent " + originalMessage.getBrokerEvent());

        Collection<Partition> partitions = partitionById.values();
        CyclicBarrier cb = new CyclicBarrier(partitions.size() + 1,
                () -> {
                    if (originalMessage.getBrokerEvent() == APPLY_CHANGES)
                        applyChanges((UInteger) originalMessage.getPojo(), false);
                    else if (originalMessage.getBrokerEvent() == UPDATE_MAXMIND_DB)
                        geoLite2UpdaterService.update();
                });

        Message<CyclicBarrier> message = new Message<>(null, null, cb, originalMessage.getBrokerEvent());
        for (Partition partition : partitions) {
            if (!putLastWithTryes(partition, message)) {
                break;
            }
        }
        if (cb.isBroken()) {
            log.warn("Failed synchronization");
            return;
        }
        log.info("Waiting synchronization");
        try {
            cb.await(5, TimeUnit.SECONDS);
        } catch (Throwable e) {
            log.warn("Exception while await synchronization", e);
            return;
        }
        log.info("Finished synchronization");
    }

    private void consumeFlush(@SuppressWarnings("rawtypes") Message message) {
        UShort portValue = message.getPort();
        PortData expectedPortData = (PortData) message.getPojo();
        PortData portData = portDataByPort.get(portValue);

        if (portData == null) // portData & storageByName already deleted after applyChanges
            return;

        // if flush from frontend or scheduler after applyChanges - registry might already be modified
        if (expectedPortData != null &&
                !portData.getProject().getId().equals(expectedPortData.getProject().getId())) {

            String logMsg = "Skip flushing sessions";
            Map<String, Storage> storageByName = playersViewByPort.get(portValue);
            if (storageByName != null) {
                int sessionsCount = 0;
                for (Storage storage : storageByName.values()) {
                    sessionsCount += storage.calcSessionsCount();
                    storage.clearStorage();
                }
                logMsg += " & removed " + declension2(storageByName.size(), "storage")
                        + " (" + declension2(sessionsCount, "session") + ")";

                /* clear & replace storageByName registry, without flush */
                storageByName.clear();
                playersViewByPort.replace(portValue, storageByNameContainerSupplier.get());
            }
            logMsg += ", due different project ids in registry after applyChanges";
            log.info(portValue + " " + logMsg);
            portData.addMessage(logMsg);
            return;
        }

        //noinspection unchecked
        message.setPojo(portData); // without creating new Message

        Partition partition = partitionByPort.get(portValue);
        putLastWithTryes(partition, message);
    }

    private void consumeFlushAndQuit(Message<?> message) {
        for (Map.Entry<Integer, Partition> entry : partitionById.entrySet()) {
            Partition partition = entry.getValue();
            putLastWithTryes(partition, message);
        }
    }

    void applyChanges(UInteger projectId, boolean updateMaxMindDb) {
        Broker broker = brokerHolder.getAvailableBrokers()
                .get(brokerHolder.getCurrentBrokerId());

        if (updateMaxMindDb)
            geoLite2UpdaterService.update();

        log.info("Updating servers settings from database, broker " + brokerToString(broker)
                + (StringUtils.isNotBlank(broker.getDescription()) ? " (" + broker.getDescription() + ")" : ""));

        FunnyRanksData funnyRanksDataSlice;
        try {
            // Slice of "now data"
            funnyRanksDataSlice = funnyRanksDao.fetchFunnyRanksData(brokerHolder.getCurrentBrokerId(),
                    projectId, messageHandlerByAppId.keySet());
        } catch (Throwable e) {
            throw new RuntimeException("Unable to fetch funnyranks data from database", e);
        }
        Map<UInteger, Game> gameByAppId = funnyRanksDataSlice.getGameByAppId();
        List<Port> portsSlice = funnyRanksDataSlice.getPorts();
        Map<UInteger, Project> projectByProjectId = funnyRanksDataSlice.getProjectByProjectId();
        Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = funnyRanksDataSlice.getDriverPropertiesByProjectId();

        // search noneMatches (removed) -> remove
        for (var iterator = portDataByPort.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            UShort portValue = entry.getKey();
            PortData portData = entry.getValue();

            //ignoring other projects if filter by projectId exists
            if (projectId != null && !projectId.equals(portData.getProject().getId()))
                continue;

            boolean portValueNotExistsInSlice = portsSlice
                    .stream()
                    .noneMatch(port -> port.getValue().equals(portValue));

            if (portValueNotExistsInSlice) {
                iterator.remove(); // remove relationship from registry
                String logMsg = "removed portData";
                Map<String, Storage> storageByName = playersViewByPort.get(portValue);
                if (storageByName != null) {
                    int sessionsCount = 0;
                    for (Storage storage : storageByName.values()) {
                        sessionsCount += storage.calcSessionsCount();
                        storage.clearStorage();
                    }
                    logMsg += " and " + declension2(storageByName.size(), "storage")
                            + " (" + declension2(sessionsCount, "session") + ")";

                    /* clear & remove storageByName registry, without flush */
                    storageByName.clear();
                    playersViewByPort.remove(portValue);
                }
                log.info(portValue + " " + logMsg);
            }
        }
        for (Port port : portsSlice) {
            UShort portValue = port.getValue();
            PortData currentPortData = portDataByPort.get(portValue);

            if (currentPortData != null) {
                // exists -> update & replace
                PortData newPortData = new PortData();
                newPortData.setGame(gameByAppId.get(port.getGameAppId()));
                newPortData.setPort(port);
                newPortData.setProject(projectByProjectId.get(port.getProjectId()));
                newPortData.setNextFlushDateTime(currentPortData.getNextFlushDateTime());
                newPortData.setDriverProperties(driverPropertiesByProjectId.get(port.getProjectId()));

                newPortData.setLastTouchDateTime(currentPortData.getLastTouchDateTime());
                newPortData.setMessages(currentPortData.getMessages());

                portDataByPort.replace(portValue, newPortData);
            } else {
                // not exists -> create
                PortData portData = new PortData();
                portData.setGame(gameByAppId.get(port.getGameAppId()));
                portData.setPort(port);
                portData.setProject(projectByProjectId.get(port.getProjectId()));
                portData.updateNextFlushDateTime();
                portData.setDriverProperties(driverPropertiesByProjectId.get(port.getProjectId()));

                portData.setLastTouchDateTime(LocalDateTime.now());

                portDataByPort.put(portValue, portData);
            }
        }
        // rebuild registry
        partitionByPort.values().forEach(Partition::clearPorts);
        partitionByPort.clear();

        List<PortData> activePortDatas = new ArrayList<>();
        for (Map.Entry<UShort, PortData> entry : portDataByPort.entrySet()) {
            UShort portValue = entry.getKey();
            PortData portData = entry.getValue();

            if (portData.isPortActive()) {
                activePortDatas.add(portData);

                Partition partition = allocatePartition(true);
                partition.addPort(portValue, true);
                partitionByPort.put(portValue, partition);

                String logMsg = "using " + partition;

                if (log.isDebugEnabled())
                    log.debug(portValue + " " + logMsg);
                portData.addMessage(logMsg);
            } else {
                Map<String, Storage> storageByName = playersViewByPort.get(portValue);
                if (storageByName != null) {
                    int sessionsCount = storageByName.values()
                            .stream()
                            .mapToInt(Storage::calcSessionsCount)
                            .sum();

                    if (sessionsCount > 0) { // if deactivated portData has sessions for flush
                        Partition partition = allocatePartition(false);
                        partition.addPort(portValue, false);
                        partitionByPort.put(portValue, partition);

                        String logMsg = "using " + partition + " for not active portData with "
                                + declension2(storageByName.size(), "storage")
                                + " (" + declension2(sessionsCount, "session") + ")";

                        if (log.isDebugEnabled())
                            log.debug(portValue + " " + logMsg);
                        portData.addMessage(logMsg);
                    }
                }
            }
        }

        for (var iterator = datagramSocketByListenerPort.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            UShort listenerPort = entry.getKey();
            DatagramSocket datagramSocket = entry.getValue();

            boolean listenerPortNotExistsInPortDatasRegistry = activePortDatas
                    .stream()
                    .noneMatch(portData -> portData.getGame().getListenerPort().equals(listenerPort));
            if (listenerPortNotExistsInPortDatasRegistry) {
                log.info("Unbinding listener port " + listenerPort);
                try {
                    datagramSocket.close();
                } catch (Throwable ignored) {
                }
                iterator.remove(); // remove relationship from registry
            }
        }
        for (PortData portData : activePortDatas) {
            Game game = portData.getGame();
            UShort listenerPort = game.getListenerPort();
            DatagramSocket datagramSocket = datagramSocketByListenerPort.get(listenerPort);
            if (datagramSocket == null) {
                log.info("Binding listener port " + listenerPort + " for game " + gameToString(game));
                try {
                    datagramSocket = new DatagramSocket(listenerPort.intValue());
                } catch (Throwable e) {
                    log.warn("Failed binding listener port " + listenerPort, e);
                }
                if (datagramSocket != null) {
                    datagramSocketByListenerPort.put(listenerPort, datagramSocket);
                    receiver.startReceiverAsync(datagramSocket);
                }
            }
        }
        changeThreadExecutorPoolSizes(receiverTE, datagramSocketByListenerPort.size());

        if (!partitionById.isEmpty()) {
            Message<?> message = new Message<>(null, null, null, TERMINATE);
            for (var partitionIterator = partitionById.values().iterator(); partitionIterator.hasNext(); ) {
                Partition partition = partitionIterator.next();
                if (partition.canRemovePartition()) { // without relationships
                    log.info("Removing " + partition);
                    partitionIterator.remove(); // remove Partition from registry
                    putLastWithTryes(partition, message); // send TERMINATE event in empty partition
                }
            }
        }
        changeThreadExecutorPoolSizes(consumerTE, partitionById.size());

        int size = portDataByPort.size();
        log.info("Registry portData: " + declension2(size, "relationship"));
        if (size > 0) {
            StringBuilder summary = portDataByPort.values()
                    .stream()
                    .collect(Collectors.groupingBy(portData -> {
                        UShort portValue = portData.getPort().getValue();

                        return partitionByPort.containsKey(portValue)
                                ? partitionByPort.get(portValue).toString() : "Without partition";
                    }, TreeMap::new, Collectors.toList()))
                    .entrySet()
                    .stream()
                    .reduce(new StringBuilder(), (sb, entry) -> {
                        String partitionName = entry.getKey();
                        sb.append("| ").append(partitionName).append('\n');
                        var portDatasByAppId = entry.getValue()
                                .stream()
                                .collect(Collectors.groupingBy(PortData::getGame,
                                        LinkedHashMap::new,
                                        Collectors.toList()));

                        for (var entry2 : portDatasByAppId.entrySet()) {
                            Game game = entry2.getKey();
                            sb.append("|- ").append(game.getName()).append("[").append(game.getAppId()).append("]").append('\n');

                            var portDatasByStatus = entry2.getValue().stream()
                                    .collect(Collectors.groupingBy(sd -> sd.isPortActive() ? "[ACTIVE]" : "[NOT ACTIVE]",
                                            LinkedHashMap::new,
                                            Collectors.toList()));
                            for (var entry3 : portDatasByStatus.entrySet()) {
                                String status = entry3.getKey();
                                sb.append("|-- ").append(status).append('\n');

                                for (PortData portData : entry3.getValue()) {
                                    sb.append("|--- ").append(portData.toString(false, false)).append('\n');
                                }
                            }
                        }
                        return sb;
                    }, StringBuilder::append);
            log.info("Partitions summary:\n" + summary);
        }
    }

    private Partition allocatePartition(boolean isActive) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (partitionById.size() < availableProcessors) {
            // add
            int partitionId = 1;
            for (; partitionId <= availableProcessors; partitionId++) {
                if (!partitionById.containsKey(partitionId))
                    break;
            }
            Partition partition = new Partition(partitionId);
            log.info("Created " + partition);

            partitionById.put(partitionId, partition);
            messagesConsumer.startConsumeAsync(partition);
            return partition;
        }
        // search by min(Partition::countPorts(isActive))
        return searchOptimalPartition(isActive);
    }

    private Partition searchOptimalPartition(boolean isActive) {
        return partitionById
                .values()
                .stream()
                .min(Comparator.comparingInt(partition -> partition.countPorts(isActive)))
                .orElseThrow(() -> new IllegalStateException("Failed to determine optimal partition"));
    }
}