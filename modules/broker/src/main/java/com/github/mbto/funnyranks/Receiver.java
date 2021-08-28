package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import static com.github.mbto.funnyranks.common.BrokerEvent.CONSUME_DATAGRAM;
import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_ALL_SESSIONS_AND_TERMINATE;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.extractPort;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.putLastWithTryes;

@Service
@Lazy(false)
@Slf4j
public class Receiver {
    @Autowired
    private BlockingDeque<Message<?>> defaultPartition;
    @Autowired
    private Map<UShort, DatagramSocket> datagramSocketByListenerPort;

    @PreDestroy
    public void destroy() {
        if (log.isDebugEnabled())
            log.debug("destroy() start");
        for (DatagramSocket datagramSocket : datagramSocketByListenerPort.values()) {
            if (datagramSocket != null) {
                try {
                    datagramSocket.close();
                } catch (Throwable ignored) {
                }
            }
        }
        for (var iterator = datagramSocketByListenerPort.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            UShort listenerPort = entry.getKey();
            DatagramSocket datagramSocket = entry.getValue();
            while (!datagramSocket.isClosed()) {
                log.info("Waiting for closing listener port " + listenerPort);
                try {
                    //noinspection BusyWait
                    Thread.sleep(300);
                } catch (Throwable ignored) {
                }
            }
            iterator.remove();
        }
        putLastWithTryes(defaultPartition, new Message<>(null, null, null, FLUSH_ALL_SESSIONS_AND_TERMINATE));
        if (log.isDebugEnabled())
            log.debug("destroy() end");
    }

    @Async("receiverTE")
    public void startReceiverAsync(DatagramSocket datagramSocket) {
        UShort listenerPort = extractPort(datagramSocket.getLocalSocketAddress());
        log.info("Activating receiver at listener port " + listenerPort);
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        for (; ; ) {
            if (datagramSocket.isClosed()) {
                log.info("Deactivation receiver at listener port " + listenerPort + " detected");
                break;
            }
            try {
                datagramSocket.receive(packet);
            } catch (Throwable e) {
                if (datagramSocket.isClosed()) {
                    log.info("Deactivation receiver at listener port " + listenerPort + " detected");
                    break;
                }
                log.warn("Exception while receive datagram packet from listener port " + listenerPort, e);
                continue;
            }
            UShort portValue = extractPort(packet.getSocketAddress());
            Message<byte[]> message = new Message<>(portValue, null, Arrays.copyOf(packet.getData(), packet.getLength()), CONSUME_DATAGRAM);
            putLastWithTryes(defaultPartition, message);
        }
        log.info("Deactivated receiver at listener port " + listenerPort);
    }
}