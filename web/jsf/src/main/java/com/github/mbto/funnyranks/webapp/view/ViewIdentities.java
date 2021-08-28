package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.identity.IdentityView;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.github.mbto.funnyranks.service.BrokerHolder;
import com.github.mbto.funnyranks.service.EventService;
import com.github.mbto.funnyranks.webapp.WebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record2;
import org.jooq.types.UShort;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_SESSIONS_FROM_FRONTEND;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildIdentitiesContainer;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.validatePort;
import static com.github.mbto.funnyranks.webapp.WebUtils.canManageProject;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

@ViewScoped
@Named
@Slf4j
public class ViewIdentities {
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private BrokerHolder brokerHolder;
    @Autowired
    private FunnyRanksDao funnyRanksDao;
    @Autowired
    private EventService eventService;

    @Getter
    @Setter
    private UShort selectedPort;
    @Getter
    @Setter
    private PortData selectedPortData;

    @Getter
    private int portsAtBroker;
    @Getter
    private int portsAtAllBrokers;
    @Getter
    private int identitiesCount;
    @Getter
    private List<IdentityView> identitiesView = Collections.emptyList();
    @Getter
    private int sessionsCount;
    @Getter
    private final Map<String, String> aggregatedIps = new HashMap<>();
    @Getter
    private final Map<String, String> aggregatedSteamIds = new HashMap<>();

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        try {
            selectedPort = UShort.valueOf(request.getParameter("port"));
            if (!validatePort(selectedPort))
                throw new RuntimeException();
        } catch (Throwable e) {
            selectedPort = null;
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid port", ""));
            return;
        }
        selectedPortData = portDataByPort.get(selectedPort);
        if (selectedPortData == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Port[" + selectedPort + "] not founded", ""));
            return;
        }
        if (WebUtils.canManageProject(selectedPortData.getProject().getId(), true, "fetchMsgs") == null) {
            selectedPortData = null;
            return;
        }
        fetchIdentitiesView();
        fetchPortsCounts();
    }

    public void fetchIdentitiesView() {
        Map<String, Storage> storageByName = playersViewByPort.get(selectedPort);
        if (storageByName == null) {
            identitiesView = Collections.emptyList();
            return;
        }
        sessionsCount = storageByName.values()
                .stream()
                .mapToInt(Storage::calcSessionsCount)
                .sum();

        Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity = buildIdentitiesContainer(selectedPortData, storageByName, true, false);
        funnyRanksDao.fillSessionByIdentityContainerWithCounties(selectedPortData, archivedSessionViewsByIdentity);

        identitiesCount = archivedSessionViewsByIdentity.size();
        identitiesView = archivedSessionViewsByIdentity
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    String identityString = entry.getKey().toString();
                    return entry.getValue()
                            .stream()
                            .sorted(Comparator.comparing(archivedSession -> archivedSession.getArchivedSession().getStarted()))
                            .map(archivedSession -> new IdentityView(identityString, archivedSession));
                }).collect(Collectors.toList());
    }

    private void fetchPortsCounts() {
        if (selectedPortData == null)
            return;
        Record2<Integer, Integer> portsCounts = funnyRanksDao
                .fetchPortsCountByAliasRecord(selectedPortData.getProject().getId(),
                        brokerHolder.getCurrentBrokerId());
        portsAtBroker = portsCounts.getValue("at_broker", Integer.class);
        portsAtAllBrokers = portsCounts.getValue("at_all_brokers", Integer.class);
    }

    public void flushIdentities() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = canManageProject(selectedPortData.getProject().getId(), true)) == null)
            return;
        log.info(selectedPort + " Flush identities received from frontend " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            eventService.flushSessions(selectedPort, FLUSH_SESSIONS_FROM_FRONTEND, !funnyRanksManager.canManageBroker());
        } catch (Throwable e) {
            log.info(selectedPort + " Flush identities not registered, " + e.getMessage()); // info, not warn
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Flush identities " + selectedPort + " not registered", e.getMessage()));
            return;
        }
        log.info(selectedPort + " Flush identities registered");
        fc.addMessage("msgs", new FacesMessage(
                "Flush identities " + selectedPort + " registered", ""));
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (Throwable ignored) {
        }
        fetchIdentitiesView();
    }
}