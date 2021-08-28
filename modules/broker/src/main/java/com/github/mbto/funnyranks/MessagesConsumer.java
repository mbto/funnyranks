package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.BrokerEvent;
import com.github.mbto.funnyranks.common.FlushEvent;
import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.Partition;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.handlers.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import static com.github.mbto.funnyranks.common.BrokerEvent.*;
import static com.github.mbto.funnyranks.common.FlushEvent.*;

@Service
@Lazy(false)
@Slf4j
public class MessagesConsumer {
    @Autowired
    private Map<UInteger, MessageHandler> messageHandlerByAppId;
    @Autowired
    private Map<UShort, PortData> portDataByPort;

    @Async("consumerTE")
    public void startConsumeAsync(Partition partition) {
        log.info(partition + " activating");
        for (; ; ) {
            Message<?> message;
            try {
                boolean debugEnabled = log.isDebugEnabled();
                if (debugEnabled)
                    log.debug(partition + " waited message");
                message = partition.getPartition().takeFirst();
                if (debugEnabled)
                    log.debug(partition + " taked message: " + message);
            } catch (Throwable e) {
                log.warn(partition + " exception while takeFirst message", e);
                continue;
            }
            if (message.getBrokerEvent() != null) {
                BrokerEvent brokerEvent = message.getBrokerEvent();
                log.info(partition + " taked brokerEvent: " + brokerEvent);
                if (brokerEvent == APPLY_CHANGES || brokerEvent == UPDATE_MAXMIND_DB) {
                    CyclicBarrier cb = (CyclicBarrier) message.getPojo();
                    if (cb.isBroken()) {
                        log.info(partition + " synchronization canceled");
                        continue;
                    }
                    log.info(partition + " ready synchronization");
                    try {
                        cb.await();
                    } catch (Throwable e) {
                        log.warn(partition + " exception while await synchronization", e);
                        continue;
                    }
                    log.info(partition + " end synchronization");
                } else if (brokerEvent == FLUSH_SESSIONS_FROM_FRONTEND) {
                    flushSessions(partition, (PortData) message.getPojo(), FRONTEND);
                } else if (brokerEvent == FLUSH_SESSIONS_FROM_SCHEDULER) {
                    flushSessions(partition, (PortData) message.getPojo(), SCHEDULER);
                } else if (brokerEvent == TERMINATE) {
                    break;
                } else if (brokerEvent == FLUSH_ALL_SESSIONS_AND_TERMINATE) {
                    partition.getPortsByStatus()
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .forEach(port -> flushSessions(partition, portDataByPort.get(port), SHUTDOWN_APPLICATION));
                    break;
                }
                continue;
            }
            handleMessage(partition, message);
        }
        log.info(partition + " deactivated");
    }

    private void handleMessage(Partition partition, Message<?> message) {
        try {
            PortData portData = (PortData) message.getPojo();
            MessageHandler messageHandler = getMessageHandler(portData);
            if (messageHandler != null) {
                messageHandler.handle(message);
            }
        } catch (Throwable e) {
            log.warn(partition + " failed handle message", e);
        }
    }

    private void flushSessions(Partition partition, PortData portData, FlushEvent flushEvent) {
        try {
            MessageHandler messageHandler = getMessageHandler(portData);
            if (messageHandler != null) {
                messageHandler.flushSessions(portData, flushEvent);
            }
        } catch (Throwable e) {
            log.warn(partition + " failed flush sessions", e);
        }
    }

    private MessageHandler getMessageHandler(PortData portData) {
        UInteger appId = portData.getGame().getAppId();
        return messageHandlerByAppId.get(appId);
    }
}