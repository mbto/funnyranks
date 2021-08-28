package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.BrokerEvent;
import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.Partition;
import com.github.mbto.funnyranks.common.dto.PortData;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_SESSIONS_FROM_SCHEDULER;
import static com.github.mbto.funnyranks.common.BrokerEvent.UPDATE_MAXMIND_DB;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.humanLifetime;

@Service
@Lazy(false)
@Slf4j
public class EventService {
    @Autowired
    private BlockingDeque<Message<?>> defaultPartition;
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Partition> partitionByPort;

    private final Map<UInteger, LocalDateTime> nextApplyChangesDateTimeByProjectId = new HashMap<>();

    @Scheduled(fixedDelay = 60 * 60 * 3 * 1000 /* 3h */, initialDelay = 60 * 60 * 3 * 1000 /* 3h */)
    public void scheduledFlushSessions() {
        for (UShort portValue : portDataByPort.keySet()) {
            try {
                flushSessions(portValue, FLUSH_SESSIONS_FROM_SCHEDULER, true);
            } catch (Throwable e) {
                log.warn("Failed send brokerEvent " + FLUSH_SESSIONS_FROM_SCHEDULER + " for port " + portValue + " in defaultPartition, due " + e);
            }
        }
    }

    @Scheduled(fixedDelay = 60 * 60 * 24 * 1000 /* 24h */, initialDelay = 60 * 60 * 24 * 1000 /* 24h */)
    public void scheduledUpdateMaxmindDB() {
        try {
            addEventToDefaultPartition(null, UPDATE_MAXMIND_DB, true);
        } catch (Throwable e) {
            log.warn("Failed send brokerEvent " + UPDATE_MAXMIND_DB + " in defaultPartition, due " + e);
        }
    }

//    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
//    public void scheduledTestScheduled() {
//        log.info("scheduledTestScheduled");
//    }

    /**
     * Apply changes in registries
     */
    public synchronized void addEventToDefaultPartition(UInteger projectId, BrokerEvent brokerEvent, boolean stopIfRecentlyAdded) throws IllegalStateException {
        LocalDateTime now = LocalDateTime.now();
        if(stopIfRecentlyAdded) {
            LocalDateTime nextApplyChangesDateTime = nextApplyChangesDateTimeByProjectId.get(projectId);
            if(nextApplyChangesDateTime != null && nextApplyChangesDateTime.isAfter(now))
                throw new RuntimeException("not available, wait " + humanLifetime(now, nextApplyChangesDateTime));
        }
        nextApplyChangesDateTimeByProjectId.put(projectId, now.plusMinutes(1));
        Message<UInteger> message = new Message<>(null, null, projectId, brokerEvent);
        defaultPartition.addFirst(message);
    }

    public void flushSessions(UShort portValue, BrokerEvent brokerEvent, boolean stopIfRecentlyFlushed) {
        PortData portData = portDataByPort.get(portValue);
        if (portData == null)
            throw new IllegalArgumentException("No portData found at port '" + portValue + "'");

        Partition partition = partitionByPort.get(portValue);
        if (partition == null) // this case only for 'dev' environment, due partitionByPort can be filled manually with ViewDashboard.makeFakes()
            throw new IllegalArgumentException("No partition found at port '" + portValue + "'");

        String logMsg;
        if (stopIfRecentlyFlushed) {
            LocalDateTime nextFlushDateTime = portData.getNextFlushDateTime();
            LocalDateTime now;
            if (nextFlushDateTime != null && nextFlushDateTime.isAfter(now = LocalDateTime.now())) {
                logMsg = "not available, wait " + humanLifetime(now, nextFlushDateTime);
                portData.addMessage("Flush sessions " + portValue + " " + logMsg);
                throw new RuntimeException(logMsg);
            }
        }
        portData.updateNextFlushDateTime();
        Message<PortData> message = new Message<>(portValue, null, portData, brokerEvent);
        try {
            defaultPartition.addFirst(message);
        } catch (Throwable e) {
            portData.addMessage("Flush sessions " + portValue + " not registered, " + e.getMessage());
            throw new RuntimeException(e);
        }
        portData.addMessage("Flush sessions " + portValue + " registered");
    }
}