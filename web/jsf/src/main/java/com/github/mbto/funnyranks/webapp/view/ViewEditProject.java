package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.github.mbto.funnyranks.service.BrokerHolder;
import com.github.mbto.funnyranks.webapp.PojoStatus;
import com.github.mbto.funnyranks.webapp.Row;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats.FUNNYRANKS_STATS;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static com.github.mbto.funnyranks.webapp.PojoStatus.*;
import static com.github.mbto.funnyranks.webapp.WebUtils.*;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ViewScoped
@Named
@Slf4j
public class ViewEditProject {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private BrokerHolder brokerHolder;
    @Autowired
    private FunnyRanksDao funnyRanksDao;

    @Getter
    private Project selectedProject;
    @Getter
    private List<Row<DriverProperty>> currentDriverPropertyRows;

    @Getter
    private int portsAtBroker;
    @Getter
    private int portsAtAllBrokers;

    @Getter
    private boolean connectionValidated;
    @Getter
    private boolean addDriverPropertyBtnDisabled;

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
        if (canManageProject(projectId, true, "fetchMsgs") == null)
            return;
        selectedProject = funnyRanksDsl.selectFrom(PROJECT)
                .where(PROJECT.ID.eq(projectId))
                .fetchOneInto(Project.class);
        if (selectedProject == null) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Project[" + projectId + "] not founded", ""));
            return;
        }

        fetchDriverProperties();
        fetchPortsCounts();
    }

    private void fetchDriverProperties() {
        currentDriverPropertyRows = funnyRanksDsl.selectFrom(DRIVER_PROPERTY)
                .where(DRIVER_PROPERTY.PROJECT_ID.eq(selectedProject.getId()))
                .orderBy(DRIVER_PROPERTY.ID.asc())
                .fetchInto(DriverProperty.class)
                .stream()
                .map(driverProperty -> new Row<>(driverProperty.getId(), driverProperty, EXISTED))
                .collect(Collectors.toList());
    }

    private void fetchPortsCounts() {
        Record2<Integer, Integer> portsCounts = funnyRanksDao
                .fetchPortsCountByAliasRecord(selectedProject.getId(), brokerHolder.getCurrentBrokerId());
        portsAtBroker = portsCounts.getValue("at_broker", Integer.class);
        portsAtAllBrokers = portsCounts.getValue("at_all_brokers", Integer.class);
    }

    public void validateDatabaseConnection() {
        log.info("Attempting to validate database connection of project " + projectToString(selectedProject) + " " + getManagerInfo());
        connectionValidated = false;
        FacesContext fc = FacesContext.getCurrentInstance();
        List<String> requiredTables = FUNNYRANKS_STATS.getTables()
                .stream()
                .map(org.jooq.Named::getName)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        List<String> tableNamesSlice;
        try (HikariDataSource hds = buildHikariDataSource(selectedProject, null)) {
            if (selectedProject.getDatabaseServerTimezone() != null)
                hds.addDataSourceProperty("serverTimezone", selectedProject.getDatabaseServerTimezone().getLiteral());

            for (Iterator<Row<DriverProperty>> iterator = currentDriverPropertyRows.iterator(); iterator.hasNext(); ) {
                Row<DriverProperty> row = iterator.next();
                DriverProperty driverProperty = row.getPojo();
                PojoStatus pojoStatus = row.getStatus();
                if (pojoStatus == TO_REMOVE && driverProperty.getId() == null) {
                    iterator.remove();
                    continue;
                }
                if (isBlank(driverProperty.getKey())) {
                    if (driverProperty.getId() != null) {
                        row.setStatus(TO_REMOVE);
                        continue;
                    }
                    iterator.remove();
                    continue;
                }
                if (pojoStatus != TO_REMOVE) {
                    if (isBlank(driverProperty.getValue()))
                        driverProperty.setValue("");
                    if (log.isDebugEnabled())
                        log.debug("\naddDataSourceProperty " + driverProperty.getKey() + "=" + driverProperty.getValue());
                    hds.addDataSourceProperty(driverProperty.getKey(), driverProperty.getValue());
                }
            }
            log.info(hikariDataSourceToString(hds));
            Field<String> tableNameField = DSL.field("TABLE_NAME", String.class).lower();
            DSLContext funnyRanksStatsDsl = configurateJooqContext(hds, 10, FUNNYRANKS_STATS.getName(), selectedProject.getDatabaseSchema());
            tableNamesSlice = funnyRanksStatsDsl.select(tableNameField)
                    .from(DSL.table("information_schema.TABLES"))
                    .where(DSL.field("TABLE_SCHEMA").eq(selectedProject.getDatabaseSchema()),
                            tableNameField.in(requiredTables))
                    .orderBy(tableNameField.asc())
                    .fetchInto(tableNameField.getType());
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation project " + projectToString(selectedProject),
                    e.toString()));
            return;
        } finally {
            addDriverPropertyBtnDisabled = false;
        }
        if (!requiredTables.equals(tableNamesSlice)) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation project " + projectToString(selectedProject),
                    "One of '" + selectedProject.getDatabaseSchema() + "' database tables is missing: "
                            + "required: " + requiredTables + ", founded: " + tableNamesSlice + ". "
                            + "You must manually import tables from *.sql files from https://github.com/mbto/funnyranks"));
            return;
        }
        fc.addMessage("msgs", new FacesMessage("Project " + projectToString(selectedProject), "settings validated"));
        connectionValidated = true;
    }

    public void saveSettings() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageProject(selectedProject.getId(), true)) == null) {
            return;
        }
        log.info("Attempting to save settings project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                localChangesCounter += pointwiseUpdateQuery(transactionalDsl, PROJECT.ID, selectedProject.getId(),
                        Arrays.asList(Pair.of(PROJECT.NAME, selectedProject.getName()),
                                Pair.of(PROJECT.DESCRIPTION, isBlank(selectedProject.getDescription()) ? null : selectedProject.getDescription()),
                                Pair.of(PROJECT.LANGUAGE, selectedProject.getLanguage()),
                                Pair.of(PROJECT.MERGE_TYPE, selectedProject.getMergeType())
                        ));
            });
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("Project " +
                    projectToString(selectedProject) + " saved", localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project " + projectToString(selectedProject),
                    e.toString()));
        }
    }

    public void saveDatabaseConnection() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageProject(selectedProject.getId(), true)) == null) {
            connectionValidated = false;
            addDriverPropertyBtnDisabled = false;
            return;
        }
        log.info("Attempting to save database connection project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                localChangesCounter += pointwiseUpdateQuery(transactionalDsl, PROJECT.ID, selectedProject.getId(),
                        Arrays.asList(
                                Pair.of(PROJECT.DATABASE_HOSTPORT, selectedProject.getDatabaseHostport()),
                                Pair.of(PROJECT.DATABASE_SCHEMA, selectedProject.getDatabaseSchema()),
                                Pair.of(PROJECT.DATABASE_USERNAME, selectedProject.getDatabaseUsername()),
                                Pair.of(PROJECT.DATABASE_PASSWORD, selectedProject.getDatabasePassword()),
                                Pair.of(PROJECT.DATABASE_SERVER_TIMEZONE, selectedProject.getDatabaseServerTimezone()))
                );
                List<Object> toRemoveDriverPropertyIds = new ArrayList<>();
                for (Iterator<Row<DriverProperty>> iterator = currentDriverPropertyRows.iterator(); iterator.hasNext(); ) {
                    Row<DriverProperty> row = iterator.next();
                    if (row.getStatus() == TO_REMOVE) {
                        if (row.getPrimaryKey() != null) {
                            toRemoveDriverPropertyIds.add(row.getPrimaryKey());
                            log.info("Updating project " + projectToString(selectedProject) + ": deleting database property " + row.getPojo()
                                    + " " + getManagerInfo(funnyRanksManager));
                        }
                        iterator.remove();
                    }
                }
                if (!toRemoveDriverPropertyIds.isEmpty()) {
                    localChangesCounter += transactionalDsl.deleteFrom(DRIVER_PROPERTY)
                            .where(DRIVER_PROPERTY.ID.in(toRemoveDriverPropertyIds))
                            .execute();
                }
                for (Row<DriverProperty> row : currentDriverPropertyRows) {
                    DriverProperty driverProperty = row.getPojo();
                    PojoStatus pojoStatus = row.getStatus();
                    if (pojoStatus == CHANGED) {
                        log.info("Updating project " + projectToString(selectedProject) + ": updating database property " + driverProperty
                                + " " + getManagerInfo(funnyRanksManager));
                        localChangesCounter += pointwiseUpdateQuery(transactionalDsl, DRIVER_PROPERTY.ID, row.getPrimaryKey(),
                                Arrays.asList(
                                        Pair.of(DRIVER_PROPERTY.KEY, driverProperty.getKey()),
                                        Pair.of(DRIVER_PROPERTY.VALUE, driverProperty.getValue())));
                    } else if (pojoStatus == NEW) {
                        log.info("Updating project " + projectToString(selectedProject) + ": inserting database property " + driverProperty
                                + " " + getManagerInfo(funnyRanksManager));
                        localChangesCounter += transactionalDsl.insertInto(DRIVER_PROPERTY)
                                .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                                .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                                .set(DRIVER_PROPERTY.PROJECT_ID, driverProperty.getProjectId())
                                .execute();
                    }
                }
            });
//            changesCounter.increment(localChangesCounter);
            fetchDriverProperties();
            fc.addMessage("msgs", new FacesMessage("Project " +
                    projectToString(selectedProject) + " saved", localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project " + projectToString(selectedProject),
                    e.toString()));
        } finally {
            connectionValidated = false;
            addDriverPropertyBtnDisabled = false;
        }
    }

    public String delete() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            return null;
        }
        log.info("Attempting to delete project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        localChangesCounter = 0;
        try {
            fetchPortsCounts();
            if (portsAtAllBrokers > 0) {
                throw new IllegalStateException("You must delete " + declension2(portsAtAllBrokers, "port")
                        + " from all brokers for delete this project");
            }
            log.info("Deleting project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
            localChangesCounter += funnyRanksDsl.deleteFrom(PROJECT)
                    .where(PROJECT.ID.eq(selectedProject.getId()))
                    .execute();
//            changesCounter.increment(localChangesCounter);
        } catch (Throwable e) {
            FacesContext.getCurrentInstance().addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed delete project " + projectToString(selectedProject),
                    e.toString()));

            return null;
        }
        return "/projects?faces-redirect=true";
    }

    public void onRowEdit(RowEditEvent event) {
        connectionValidated = false;
        //noinspection unchecked
        Row<DriverProperty> row = (Row<DriverProperty>) event.getObject();
        if (row.getPrimaryKey() != null) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if (currentDriverPropertyRows.get(currentDriverPropertyRows.size() - 1).equals(row)) {
            addDriverPropertyBtnDisabled = false;
        }
        if (log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddProperty() {
        if (log.isDebugEnabled())
            log.debug("\nonAddProperty");

        connectionValidated = false;

        DriverProperty driverProperty = new DriverProperty();
        driverProperty.setProjectId(selectedProject.getId());
        currentDriverPropertyRows.add(new Row<>(driverProperty, NEW));
        addDriverPropertyBtnDisabled = true;
    }

    public void onRestoreProperty(Row<DriverProperty> row) {
        connectionValidated = false;

        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if (log.isDebugEnabled())
            log.debug("\nonRestoreProperty " + row);
    }

    public void onRemoveProperty(Row<DriverProperty> row) {
        connectionValidated = false;

        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if (log.isDebugEnabled())
            log.debug("\nonRemoveProperty " + row);
    }
}