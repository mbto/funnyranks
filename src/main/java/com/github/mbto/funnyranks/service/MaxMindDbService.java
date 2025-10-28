package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.utils.geoip.GeoInfo;
import com.github.mbto.funnyranks.common.utils.geoip.GeoIpFormatter;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.github.mbto.funnyranks.common.utils.ProjectUtils.extractFileSize;

@Service
@Slf4j
public class MaxMindDbService {
    private final String maxmindCityMMDBFilepath;

    public MaxMindDbService(@Value("${maxmind.city.mmdb.filepath}") String maxmindCityMMDBFilepath) {
        this.maxmindCityMMDBFilepath = maxmindCityMMDBFilepath;
    }

    private Path dbPath;
    private DatabaseReader maxmindDbReader;
    private long lastSizeOfDb;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    public void init() {
        try {
            dbPath = Paths.get(maxmindCityMMDBFilepath);
        } catch (Throwable e) {
            log.warn("Failed open maxmind db, due maxmindCityMMDBFilepath=" + maxmindCityMMDBFilepath, e);
            return;
        }
        reopenMaxmindDbReader();
    }

    @PreDestroy
    public void destroy() {
        if(maxmindDbReader != null) {
            try {
                maxmindDbReader.close();
            } catch (Throwable ignored) {}
        }
    }

    public void fillSessionByIdentityContainerWithGeoInfos(PortData portData,
                                                           Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity) {
        if (dbPath == null) {
            return;
        }
        lock.readLock().lock();
        try {
            if (maxmindDbReader != null && isDbUpToDate()) {
                syncFill(portData, archivedSessionViewsByIdentity);
                return;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (maxmindDbReader == null || !isDbUpToDate()) {
                if (!reopenMaxmindDbReader()) {
                    return;
                }
            }
            lock.readLock().lock(); // downgrade: take readLock, release writeLock
        } finally {
            lock.writeLock().unlock();
        }
        try {
            if (maxmindDbReader != null) {
                syncFill(portData, archivedSessionViewsByIdentity);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isDbUpToDate() {
        if (maxmindDbReader == null || !Files.exists(dbPath)) {
            return false;
        }
        long currentSize = extractFileSize(dbPath);
        return lastSizeOfDb == currentSize;
    }

    private void syncFill(PortData portData,
                         Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity) {
        List<UInteger> uniqueIps = archivedSessionViewsByIdentity.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(archivedSessionView -> !archivedSessionView.getArchivedSession().isGeoInfoSetterInvoked()
                                               && archivedSessionView.getArchivedSession().getIp() != null)
                .map(archivedSessionView -> archivedSessionView.getArchivedSession().getIp())
                .distinct()
                .toList();
        UShort portValue = portData.getPort().getValue();
        if (uniqueIps.isEmpty()) {
            log.info(portValue + " Skipping fetch data from MaxMind GeoLite2 city database, due empty uniqueIps");
            return;
        }
        log.info(portValue + " Fetching data from MaxMind GeoLite2 city database");
        Map<UInteger, GeoInfo> geoInfoByIp = new HashMap<>();
        for (UInteger uniqueIp : uniqueIps) {
            GeoInfo geoInfo;
            try {
                geoInfo = GeoIpFormatter.buildGeoInfoByIp(maxmindDbReader, uniqueIp);
            } catch (Throwable ignored) {
                continue;
            }
            geoInfoByIp.put(uniqueIp, geoInfo);
        }
        archivedSessionViewsByIdentity.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(ags -> !ags.getArchivedSession().isGeoInfoSetterInvoked())
                .forEach(archivedSessionView -> {
                    Session session = archivedSessionView.getArchivedSession();
                    UInteger ip = session.getIp();
                    if (ip == null) {
                        session.setGeoInfoSetterInvoked(true);
                        return;
                    }
                    GeoInfo geoInfo = geoInfoByIp.get(ip);
                    if (geoInfo == null) {
                        session.setGeoInfoSetterInvoked(true);
                        return;
                    }
                    session.setGeoInfo(geoInfo);
                });
    }

    private boolean reopenMaxmindDbReader() {
        if(maxmindDbReader != null) {
            try {
                maxmindDbReader.close();
            } catch (Throwable ignored) {}
        }
        log.info("Reopening maxmind db from dbPath=" + dbPath.toAbsolutePath());
        try {
            maxmindDbReader = new DatabaseReader
                 .Builder(dbPath.toFile())
                 .withCache(new CHMCache())
                 .fileMode(Reader.FileMode.MEMORY)
                 .build();
        } catch (Throwable e) {
            maxmindDbReader = null;
            lastSizeOfDb = 0;
            log.warn("Failed reopen maxmind db from dbPath=" + dbPath.toAbsolutePath(), e);
            return false;
        }
        lastSizeOfDb = extractFileSize(dbPath);
        return true;
    }
}