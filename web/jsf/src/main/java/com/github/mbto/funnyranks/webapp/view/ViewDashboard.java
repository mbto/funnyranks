package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.service.BrokerHolder;
import com.github.mbto.funnyranks.service.EventService;
import com.github.mbto.funnyranks.webapp.DependentUtil;
import com.github.mbto.funnyranks.webapp.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.types.UShort;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.BrokerEvent.APPLY_CHANGES;
import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_SESSIONS_FROM_FRONTEND;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.forDashboardPortDataComparator;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

@ViewScoped
@Named
@Slf4j
public class ViewDashboard {
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private DependentUtil util;
    @Autowired
    private BrokerHolder brokerHolder;
    @Autowired
    private EventService eventService;

    @Getter private int processors;
    @Getter private String freeMemory;
    @Getter private String maxMemory;
    @Getter private String allocatedMemory;
    @Getter private String totalFreeMemory;

    @Getter
    private List<PortData> sortedPortData;
    @Getter
    private int sessionsCount;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        calculateMemory();
        updateSortedPortData();
    }

    public void calculateMemory() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryL = runtime.freeMemory();
        long maxMemoryL = runtime.maxMemory();
        long allocatedMemoryL = runtime.totalMemory();
        processors = runtime.availableProcessors();
        freeMemory = String.format("%.2f", freeMemoryL / 1024f / 1024f);
        maxMemory = String.format("%.2f", maxMemoryL / 1024f / 1024f);
        allocatedMemory = String.format("%.2f", allocatedMemoryL / 1024f / 1024f);
        totalFreeMemory = String.format("%.2f", (freeMemoryL + (maxMemoryL - allocatedMemoryL)) / 1024f / 1024f);
    }

    public void updateSortedPortData() {
        if (log.isDebugEnabled())
            log.debug("\nupdateSortedPortData");
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = WebUtils.extractFunnyRanksManager()) == null)
            return;
        sessionsCount = 0;
        sortedPortData = portDataByPort
                .values()
                .stream()
                .filter(portData -> funnyRanksManager.canManageProject(portData.getProject().getId()))
                .sorted(forDashboardPortDataComparator)
                .peek(portData -> sessionsCount += getSessionsCountByPort(portData.getPort().getValue()))
                .collect(Collectors.toList());
    }

    public int getSessionsCountByPort(UShort portValue) {
        Map<String, Storage> storageByName = playersViewByPort.get(portValue);
        if (storageByName == null || storageByName.isEmpty())
            return 0;
        return storageByName.values()
                .stream()
                .mapToInt(Storage::calcSessionsCount)
                .sum();
    }

    public void onRowSelect(SelectEvent event) {
        Object object = event.getObject();
        if (log.isDebugEnabled())
            log.debug("\nonRowSelect " + object);
        UShort portValue = ((PortData) object).getPort().getValue();
        util.sendRedirect("identities?port=" + portValue);
    }

    public void flushSessions() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = WebUtils.extractFunnyRanksManager(true)) == null)
            return;
        log.info("Flush sessions received from frontend " + WebUtils.getManagerInfo(funnyRanksManager));
        List<String> infoMsgs = new ArrayList<>();
        List<String> warnMsgs = new ArrayList<>();
        for (PortData portData : sortedPortData) {
            UShort portValue = portData.getPort().getValue();
            try {
                eventService.flushSessions(portValue, FLUSH_SESSIONS_FROM_FRONTEND, !funnyRanksManager.canManageBroker());
            } catch (Throwable e) {
                log.info(portValue + " Flush sessions not registered, " + e.getMessage()); // info, not warn
                warnMsgs.add("Flush sessions " + portValue + " not registered, " + e.getMessage());
                continue;
            }
            log.info(portValue + " Flush sessions registered");
            infoMsgs.add("Flush sessions " + portValue + " registered");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (!infoMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, String.join("<br/>", infoMsgs), ""));
        if (!warnMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, String.join("<br/>", warnMsgs), ""));
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (Throwable ignored) {
        }
        updateSortedPortData();
    }

    public void applyBrokerChanges() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = WebUtils.canManageBroker(true)) == null)
            return;
        log.info("Apply broker changes received from frontend " + WebUtils.getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            eventService.addEventToDefaultPartition(null, APPLY_CHANGES, false);
        } catch (Throwable e) {
            String msg = "Apply broker changes not registered, " + e.getMessage();
            log.info(msg); // info, not warn
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, msg, ""));
            return;
        }
        fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, "Apply broker changes registered", ""));
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (Throwable ignored) {
        }
        updateSortedPortData();
    }

    public void makeFakes() {
        if (!brokerHolder.isDevEnvironment())
            return;
        Map<UShort, PortData> portDataByPortFromSorted = sortedPortData.stream()
                .collect(Collectors.toMap(portData -> portData.getPort().getValue(), Function.identity()));
        ProjectUtils.fillFakes(portDataByPortFromSorted, playersViewByPort);
        updateSortedPortData();
    }
}