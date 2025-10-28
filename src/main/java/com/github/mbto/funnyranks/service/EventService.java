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

//    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24  /* 24h */, initialDelay = 1000 * 60 * 60 * 24 /* 24h */)
    @Scheduled(cron = "0 0 9 * * *") // 09:00
    public void scheduledFlushSessions() {
        log.info("Starting flush sessions by scheduler");
        boolean isDebugEnabled = log.isDebugEnabled();
        for (Map.Entry<UShort, PortData> entry : portDataByPort.entrySet()) {
            UShort portValue = entry.getKey();
            PortData portData = entry.getValue();
            if(portData.getPort().getStartSessionOnAction()) {
                // guns with maps rotating -> flushes by change map event
                continue;
            }
            // kreedz with maps rotating or knife with 24/7 -> flushes by scheduler
            try {
                flushSessions(portValue, FLUSH_SESSIONS_FROM_SCHEDULER, true);
            } catch (Throwable e) {
                if(portData.isPortActive()) {
                    log.warn("Failed send brokerEvent " + FLUSH_SESSIONS_FROM_SCHEDULER + " for port " + portValue + " in defaultPartition, due " + e);
                } else if(isDebugEnabled) {
                    log.debug("Failed send brokerEvent " + FLUSH_SESSIONS_FROM_SCHEDULER + " for port " + portValue + " in defaultPartition, due " + e + " (port not active)");
                }
            }
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
        // this case if partition not setted (port disabled), or dev:partitionByPort filled manually with ViewDashboard.makeFakes()
        if (partition == null) {
            throw new IllegalArgumentException("No partition found at port '" + portValue + "'");
        }

        String logMsg;
        if (stopIfRecentlyFlushed) {
            LocalDateTime nextFlushDateTime = portData.getNextFlushDateTime();
            LocalDateTime now = LocalDateTime.now();
            if (nextFlushDateTime != null && nextFlushDateTime.isAfter(now)) {
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