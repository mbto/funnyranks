package com.github.mbto.funnyranks.handlers;

import com.github.mbto.funnyranks.SessionsSender;
import com.github.mbto.funnyranks.common.FlushEvent;
import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.dto.session.StorageFetchMode;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import java.time.LocalDateTime;
import java.util.Map;

import static com.github.mbto.funnyranks.common.Constants.storageByNameContainerSupplier;
import static com.github.mbto.funnyranks.common.FlushEvent.SHUTDOWN_APPLICATION;
import static com.github.mbto.funnyranks.common.dto.session.StorageFetchMode.*;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.declension2;

/**
 * For developers: You can add yours game packet data converter/validator/others, extends this class
 */
@Setter
@Getter
@Slf4j
public abstract class MessageHandler {
    private Map<UShort, Map<String, Storage>> playersViewByPort;
    private SessionsSender sessionsSender;

    /**
     * app_id from `funnyranks`.`game` table
     * https://developer.valvesoftware.com/wiki/Steam_Application_IDs#Server_Files
     * https://api.steampowered.com/ISteamApps/GetAppList/v2/
     */
    public abstract UInteger getAppId();

    public abstract boolean validate(UShort portValue, byte[] data);

    public abstract String convert(UShort portValue, byte[] data);

    public abstract void handle(Message<?> message);

    public void countFrag(Port port,
                          LocalDateTime dateTime,
                          String killerName, String killerAuth,
                          String victimName, String victimAuth) {
        Storage killerStorage = allocateStorage(port, killerName, killerAuth, dateTime);
        killerStorage.getSession(dateTime).upKills();

        Storage victimStorage = allocateStorage(port, victimName, victimAuth, dateTime);
        victimStorage.getSession(dateTime).upDeaths();
    }

    public Map<String, Storage> allocateStorageByNameContainer(UShort portValue, StorageFetchMode storageFetchMode) {
        if (storageFetchMode == DONT_CREATE) {
            return playersViewByPort.get(portValue);
        } else if (storageFetchMode == CREATE_IF_NULL) {
            Map<String, Storage> storageByName = playersViewByPort.get(portValue);
            if (storageByName == null) {
                storageByName = storageByNameContainerSupplier.get();
                playersViewByPort.put(portValue, storageByName);
                log.info(portValue + " Created storageByName registry");
            }
            return storageByName;
        } else if (storageFetchMode == REPLACE_IF_EXISTS) {
            Map<String, Storage> oldStorageByName = playersViewByPort.replace(portValue, storageByNameContainerSupplier.get());
            if (oldStorageByName != null)
                log.info(portValue + " Recreated storageByName registry");
            return oldStorageByName; /* return old registry, for flush */
        } else if (storageFetchMode == REMOVE) {
            return playersViewByPort.remove(portValue); /* return old registry, for flush */
        }
        throw new UnsupportedOperationException("Unsupported storageFetchMode '" + storageFetchMode + "'");
    }

    public Storage allocateStorage(Port port, String name, String steamId, LocalDateTime dateTime) {
        return allocateStorage(port, name, null, steamId, dateTime);
    }

    public Storage allocateStorage(Port port, String name, String newName, String steamId, LocalDateTime dateTime) {
        UShort portValue = port.getValue();
        Map<String, Storage> storageByName = allocateStorageByNameContainer(portValue, CREATE_IF_NULL);
        Storage storage = storageByName.get(name);
        boolean nameChangeEvent = newName != null;
        if (storage == null) {
            String key;
            if (nameChangeEvent) {
                storage = storageByName.get(newName);
                key = newName;
            } else
                key = name;
            if (storage != null) {
                log.info(portValue + " Used storage " + key + ", " + steamId);
            } else {
                storage = new Storage();
                storageByName.put(key, storage);
                log.info(portValue + " Created storage " + key + ", " + steamId);
            }
            if (nameChangeEvent && !port.getStartSessionOnAction()) {
                storage.getSession(true).setStarted(dateTime);
            }
        } else if (nameChangeEvent) {
            /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
            //noinspection StringOperationCanBeSimplified
            if (name.toLowerCase().equals(newName.toLowerCase())) {
                storageByName.put(newName, storageByName.remove(name));
            } else {
                Session session = storage.getSession(false);
                UInteger ip = null;
                if (session != null) {
                    session.setSteamId64(steamId);
                    ip = session.getIp();
                }
                storage.onDisconnected(dateTime);

                Storage anotherStorage = allocateStorage(port, newName, null, steamId, dateTime);
                Session anotherSession = port.getStartSessionOnAction()
                        ? anotherStorage.getSession(true) : anotherStorage.getSession(dateTime);
                if (ip != null)
                    anotherSession.setIp(ip);
                return anotherStorage;
            }
        }
        storage.getSession(true).setSteamId64(steamId);
        return storage;
    }

    public void flushSessions(PortData portData, FlushEvent flushEvent) {
        flushSessions(portData, null, flushEvent);
    }

    public void flushSessions(PortData portData, LocalDateTime dateTime, FlushEvent flushEvent) {
        UShort portValue = portData.getPort().getValue();
        String logMsg = "Started flush sessions by flushEvent '" + flushEvent + "'";
        log.info(portValue + " " + logMsg);
        portData.addMessage(logMsg);
        Map<String, Storage> storageByName = allocateStorageByNameContainer(portValue,
                flushEvent == SHUTDOWN_APPLICATION ? REMOVE : REPLACE_IF_EXISTS);
        if (storageByName == null || storageByName.isEmpty()) {
            logMsg = "Skip flush sessions, due empty storageByName registry or not exists";
            log.info(portValue + " " + logMsg);
            portData.addMessage(logMsg);
            return;
        }
        Map<String, Storage> storageByNameCopy = storageByNameContainerSupplier.get();
        storageByNameCopy.putAll(storageByName);
        storageByName.clear();

        int storagesCount = storageByNameCopy.size();
        logMsg = "Prepared " + declension2(storagesCount, "storage") + " to flush";
        log.info(portValue + " " + logMsg);
        portData.addMessage(logMsg);
        if (dateTime == null) {
            dateTime = portData.getLastTouchDateTime();
        }
        for (Storage storage : storageByNameCopy.values()) {
            storage.onDisconnected(dateTime);
        }
        // after this call use of registry in aggregateAndMergeAsync onwards may be outdated
        sessionsSender.aggregateAndMergeAsync(portData, storageByNameCopy);
    }
    public boolean isAuthBOT(String auth) {
        return "BOT".equalsIgnoreCase(auth);
    }
    /**
     * L 01/08/2021 - 19:48:23: [REUNION]: HLTV Proxy (127.0.0.1) authorized as HLTV
     * L 01/08/2021 - 19:48:23: "HLTV Proxy<56><HLTV><>" connected, address "127.0.0.1:27020"
     * L 01/08/2021 - 19:48:24: "HLTV Proxy<56><HLTV><>" entered the game
     * L 01/08/2021 - 19:48:24: "HLTV Proxy<56><HLTV><>" joined team "SPECTATOR"
     * L 01/08/2021 - 19:48:26: "HLTV Proxy<56><HLTV><SPECTATOR>" disconnected
     * Dropped HLTV Proxy from server
     * Reason:  Client sent 'drop'
     */
    public boolean isAuthHLTV(String auth) {
        return "HLTV".equalsIgnoreCase(auth);
    }
}