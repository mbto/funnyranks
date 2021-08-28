package com.github.mbto.funnyranks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.ThreadLocalRandom.current;

@Profile("test")
@Component
@Slf4j
public class LogsSender {
    @SneakyThrows
    public void sendLogs(String fileName, int portStart, int portEnd) {
        List<String> logs;

        try (InputStream resourceAsStream = LogsSender.class.getResourceAsStream("/example_logs/" + fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            logs = br.lines()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }

        String ip = "127.0.0.1";
        InetSocketAddress serv1 = new InetSocketAddress(ip, 27499);

        CompletableFuture<Void>[] tasks = IntStream.rangeClosed(portStart, portEnd)
                .boxed()
                .map(port -> CompletableFuture.runAsync(() -> {
                    boolean debugEnabled = log.isDebugEnabled();

                    try (DatagramSocket socket = new DatagramSocket(port)) {
                        for (String payload : logs) {
                            if (debugEnabled) {
                                log.debug(port + " Sending payload: " + payload);
                            }

                            /* L 01/08/2021 - 13:15:00: "Name1<5><STEAM_ID_LAN><>" connected, address "12.12.12.12:27005" */
                            byte[] rawSource = payload.getBytes();
                            byte[] rawPayload = new byte[rawSource.length + 8]; // "-1 -1 -1 -1 l o g  "
                            Arrays.fill(rawPayload, 0, 5, (byte) -1);
                            rawPayload[4] = 'l';
                            rawPayload[5] = 'o';
                            rawPayload[6] = 'g';
                            rawPayload[7] = ' ';
                            System.arraycopy(rawSource, 0, rawPayload, 8, rawPayload.length - 8);

                            DatagramPacket datagramPacket = new DatagramPacket(rawPayload, 0, rawPayload.length, serv1);
                            datagramPacket.setSocketAddress(serv1);
                            socket.send(datagramPacket);

                            Thread.sleep(current().nextInt(1, 15));
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                })).<CompletableFuture<Void>>toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tasks).join();
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }
}
