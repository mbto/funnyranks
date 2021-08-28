package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.service.BrokerHolder;
import com.github.mbto.funnyranks.service.EventService;
import com.github.mbto.funnyranks.webapp.PojoStatus;
import com.github.mbto.funnyranks.webapp.Row;
import com.github.mbto.funnyranks.webapp.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.BrokerEvent.APPLY_CHANGES;
import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_SESSIONS_FROM_FRONTEND;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Game.GAME;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static com.github.mbto.funnyranks.webapp.PojoStatus.*;
import static com.github.mbto.funnyranks.webapp.WebUtils.*;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ViewScoped
@Named
@Slf4j
public class ViewPortsByProjectId {
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private BrokerHolder brokerHolder;
    @Autowired
    private EventService eventService;

    @Getter
    private Project selectedProject;

    @Getter
    private Map<UInteger, Game> gameByAppId;
    @Getter
    private final List<Row<Port>> currentRows = new ArrayList<>();
    @Getter
    private final Map<UInteger, List<Row<Port>>> rowsByBrokerId = new LinkedHashMap<>();
    @Getter
    private String existedPorts;
    @Getter
    private int totalSessionsCount;
    @Getter
    private final Map<UShort, Integer> projectSessionsCountByPort = new HashMap<>();

    @Getter
    private boolean addServerBtnDisabled;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String projectIdStr = request.getParameter("projectId");
        FacesContext fc = FacesContext.getCurrentInstance();
        UInteger projectId;
        try {
            projectId = UInteger.valueOf(projectIdStr);
        } catch (Throwable e) {
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid projectId", ""));
            return;
        }
        if (WebUtils.canManageProject(projectId, true, "fetchMsgs") == null)
            return;
        selectedProject = funnyRanksDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .where(PROJECT.ID.eq(projectId))
                .fetchOneInto(Project.class);
        if (selectedProject == null) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Project[" + projectIdStr + "] not founded", ""));
            return;
        }
        fetchPorts();
        updateProjectSessionsCountByPort();
    }

    private void fetchPorts() {
        List<Row<Port>> portRows = funnyRanksDsl.transactionResult(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            gameByAppId = transactionalDsl.select(GAME.APP_ID, GAME.NAME)
                    .from(GAME)
                    .fetchMap(GAME.APP_ID, Game.class);
            return transactionalDsl.selectFrom(PORT)
                    .where(PORT.PROJECT_ID.eq(selectedProject.getId()))
                    .orderBy(PORT.BROKER_ID.desc(), PORT.ID.asc())
                    .fetchInto(Port.class)
                    .stream()
                    .map(port -> new Row<>(port.getId(), port, EXISTED))
                    .collect(Collectors.toList());
        });
        currentRows.clear();
        rowsByBrokerId.clear();
        for (Row<Port> portRow : portRows) {
            Port port = portRow.getPojo();
            if (port.getBrokerId().equals(brokerHolder.getCurrentBrokerId())) {
                currentRows.add(portRow);
            } else {
                List<Row<Port>> rows = rowsByBrokerId.get(port.getBrokerId());
                //noinspection Java8MapApi
                if (rows == null) {
                    rows = new ArrayList<>();
                    rowsByBrokerId.put(port.getBrokerId(), rows);
                }
                rows.add(portRow);
            }
        }
        fetchExistedPorts();
    }

    private void updateProjectSessionsCountByPort() {
        totalSessionsCount = 0;
        projectSessionsCountByPort.clear();
        for (Row<Port> currentBrokerRow : currentRows) {
            UShort portValue = currentBrokerRow.getPojo().getValue();
            if (portValue == null)
                continue;
            Map<String, Storage> storageByName = playersViewByPort.get(portValue);
            if (storageByName == null)
                continue;
            int sessionsCount = storageByName.values()
                    .stream()
                    .mapToInt(Storage::calcSessionsCount)
                    .sum();
            projectSessionsCountByPort.put(portValue, sessionsCount);
            totalSessionsCount += sessionsCount;
        }
    }

    private void fetchExistedPorts() {
        existedPorts = funnyRanksDsl.select(
                        DSL.groupConcat(PORT.VALUE)
                                .orderBy(PORT.VALUE.asc())
                                .separator("<br/>")
                                .as("values")
                ).from(PORT)
                .where(PORT.PROJECT_ID.notEqual(selectedProject.getId()),
                        PORT.BROKER_ID.eq(brokerHolder.getCurrentBrokerId())
                ).fetchOneInto(String.class);
    }

    public void flushProjectSessions() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = canManageProject(selectedProject.getId(), true)) == null)
            return;
        log.info("Flush sessions of project " + projectToString(selectedProject) + " received from frontend " + getManagerInfo(funnyRanksManager));
        List<String> infoMsgs = new ArrayList<>();
        List<String> warnMsgs = new ArrayList<>();
        for (UShort portValue : playersViewByPort.keySet()) {
            PortData portData = portDataByPort.get(portValue);
            if (portData == null || !portData.getProject().getId().equals(selectedProject.getId()))
                continue;
            Map<String, Storage> storageByName = playersViewByPort.get(portValue);
            if (storageByName == null || storageByName.isEmpty()) {
                String logMsg = "Skip flush sessions, due empty storageByName registry or not exists";
                log.info(portValue + " " + logMsg);
                portData.addMessage(logMsg);
                continue;
            }
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
        updateProjectSessionsCountByPort();
    }

    public void applyProjectChanges() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = canManageProject(selectedProject.getId(), true)) == null)
            return;
        log.info("Apply changes for project " + projectToString(selectedProject) + " received from frontend " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            eventService.addEventToDefaultPartition(selectedProject.getId(), APPLY_CHANGES, !funnyRanksManager.canManageBroker());
        } catch (Throwable e) {
            String msg = "Apply changes for project " + projectToString(selectedProject) + " not registered, " + e.getMessage();
            log.info(msg); // info, not warn
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, msg, ""));
            return;
        }
        fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, "Apply changes for project " + projectToString(selectedProject) + " registered", ""));
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (Throwable ignored) {
        }
        updateProjectSessionsCountByPort();
    }

    public void validatePortValue(FacesContext context, UIComponent component, String value) throws ValidatorException {
        UShort portValue;
        try {
            portValue = UShort.valueOf(value);
            if (!validatePort(portValue))
                throw new RuntimeException();
        } catch (Throwable e) {
            throw makeValidatorException(value, "");
        }
        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");
        if (log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentRows.size=" + currentRows.size());
        for (int i = 0; i < currentRows.size(); i++) {
            if (i == rowIndexVar)
                continue;
            Row<Port> row = currentRows.get(i);
            Port port = row.getPojo();
            if (portValue.equals(port.getValue()))
                throw makeValidatorException(value, "Port value already exists at port " + portToString(port));
        }
        Project projectWithSamePort = funnyRanksDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .join(PORT).on(PROJECT.ID.eq(PORT.PROJECT_ID))
                .where(PORT.VALUE.eq(portValue),
                        PORT.PROJECT_ID.notEqual(selectedProject.getId()),
                        PORT.BROKER_ID.eq(brokerHolder.getCurrentBrokerId())
                ).groupBy(PROJECT.ID)
                .fetchOneInto(Project.class);
        if (projectWithSamePort != null) {
            throw makeValidatorException(value, "This port belongs to another project at same broker");
        }
    }

    public void save() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageProject(selectedProject.getId(), true)) == null) {
            addServerBtnDisabled = false;
            return;
        }
        log.info("Attempting to save ports of project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            List<Object> toRemovePortIds = new ArrayList<>();
            for (Iterator<Row<Port>> iterator = currentRows.iterator(); iterator.hasNext(); ) {
                Row<Port> row = iterator.next();
                Port port = row.getPojo();
                if (row.getStatus() == TO_REMOVE) {
                    if (row.getPrimaryKey() != null) {
                        toRemovePortIds.add(row.getPrimaryKey());
                        log.info("Updating project " + projectToString(selectedProject) + ": deleting port " + portToString(port)
                                + " " + getManagerInfo(funnyRanksManager));
                    }
                    iterator.remove();
                } else if (row.getStatus() == NEW) {
                    if (port.getValue() == null || isBlank(port.getName())) {
                        iterator.remove();
                    }
                }
            }
            if (!toRemovePortIds.isEmpty() || !currentRows.isEmpty()) {
                funnyRanksDsl.transaction(config -> {
                    DSLContext transactionalDsl = DSL.using(config);
                    if (!toRemovePortIds.isEmpty()) {
                        localChangesCounter += transactionalDsl.deleteFrom(PORT)
                                .where(PORT.ID.in(toRemovePortIds))
                                .execute();
                    }
                    for (Row<Port> row : currentRows) {
                        Port port = row.getPojo();
                        PojoStatus pojoStatus = row.getStatus();
                        if (pojoStatus == CHANGED) {
                            log.info("Updating project " + projectToString(selectedProject) + ": updating port " + portToString(port)
                                    + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += pointwiseUpdateQuery(transactionalDsl, PORT.ID, row.getPrimaryKey(),
                                    Arrays.asList(
                                            Pair.of(PORT.GAME_APP_ID, port.getGameAppId()),
                                            Pair.of(PORT.VALUE, port.getValue()),
                                            Pair.of(PORT.NAME, port.getName()),
                                            Pair.of(PORT.ACTIVE, port.getActive()),
                                            Pair.of(PORT.FFA, port.getFfa()),
                                            Pair.of(PORT.IGNORE_BOTS, port.getIgnoreBots()),
                                            Pair.of(PORT.START_SESSION_ON_ACTION, port.getStartSessionOnAction()))
                            );
                        } else if (pojoStatus == NEW) {
                            log.info("Updating project " + projectToString(selectedProject) + ": inserting port " + portToString(port)
                                    + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += transactionalDsl.insertInto(PORT)
                                    .set(PORT.BROKER_ID, port.getBrokerId())
                                    .set(PORT.PROJECT_ID, port.getProjectId())
                                    .set(PORT.GAME_APP_ID, port.getGameAppId())
                                    .set(PORT.VALUE, port.getValue())
                                    .set(PORT.NAME, port.getName())
                                    .set(PORT.ACTIVE, port.getActive())
                                    .set(PORT.FFA, port.getFfa())
                                    .set(PORT.IGNORE_BOTS, port.getIgnoreBots())
                                    .set(PORT.START_SESSION_ON_ACTION, port.getStartSessionOnAction())
                                    .execute();
                        }
                    }
                });
            }
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("Project " + projectToString(selectedProject) + " saved",
                    localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project " + projectToString(selectedProject),
                    e.toString()));
        } finally {
            addServerBtnDisabled = false;
            fetchPorts();
        }
    }

    public void onRowEdit(RowEditEvent event) {
        //noinspection unchecked
        Row<Port> row = (Row<Port>) event.getObject();
        if (row.getPrimaryKey() != null) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if (currentRows.get(currentRows.size() - 1).equals(row)) {
            addServerBtnDisabled = false;
        }
        if (log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddPort() {
        if (log.isDebugEnabled())
            log.debug("\nonAddPort");

        fetchExistedPorts();
        Port port = new Port();
        port.setBrokerId(brokerHolder.getCurrentBrokerId());
        port.setProjectId(selectedProject.getId());
        currentRows.add(new Row<>(port, NEW));
        addServerBtnDisabled = true;
    }

    public void onRestoreRow(Row<Port> row) {
        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if (log.isDebugEnabled())
            log.debug("\nonRestoreRow " + row);
    }

    public void onRemoveRow(Row<Port> row) {
        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if (log.isDebugEnabled())
            log.debug("\nonRemoveRow " + row);
    }
}