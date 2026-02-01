package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.dto.session.IpWrapper;
import com.github.mbto.funnyranks.common.utils.geoip.GeoInfo;
import com.github.mbto.funnyranks.common.utils.geoip.GeoIpFormatter;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
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

    public void fillIpWrappersWithGeoInfos(List<IpWrapper> ipWrappers, boolean isGameBrowser) {
        if (dbPath == null) {
            return;
        }
        lock.readLock().lock();
        try {
            if (maxmindDbReader != null && isDbUpToDate()) {
                syncFill(ipWrappers, isGameBrowser);
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
                syncFill(ipWrappers, isGameBrowser);
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

    private void syncFill(List<IpWrapper> ipWrappers, boolean isGameBrowser) {
        boolean isDebugEnabled = log.isDebugEnabled();
        if (ipWrappers.isEmpty()) {
            if(isDebugEnabled) {
                log.debug("Skipping fetch data from MaxMind GeoLite2 city database, due empty ipWrappers");
            }
            return;
        }
        if(isDebugEnabled) {
            log.debug("Fetching data from MaxMind GeoLite2 city database");
        }
        for (IpWrapper ipWrapper : ipWrappers) {
            UInteger ip = ipWrapper.getIp();
            if(ip == null) {
                continue;
            }
            GeoInfo geoInfo;
            try {
                geoInfo = GeoIpFormatter.buildGeoInfoByIp(maxmindDbReader, ip, isGameBrowser);
            } catch (Throwable e) {
//                if(isDebugEnabled) {
//                    log.debug("Failed buildGeoInfoByIp for ip=" + ip, e);
//                }
                continue;
            }
            ipWrapper.setGeoInfo(geoInfo);
        }
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