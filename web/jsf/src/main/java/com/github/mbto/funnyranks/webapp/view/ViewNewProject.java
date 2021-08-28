package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectDatabaseServerTimezone;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.webapp.PojoStatus;
import com.github.mbto.funnyranks.webapp.Row;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats.FUNNYRANKS_STATS;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static com.github.mbto.funnyranks.webapp.PojoStatus.NEW;
import static com.github.mbto.funnyranks.webapp.PojoStatus.TO_REMOVE;
import static com.github.mbto.funnyranks.webapp.WebUtils.canManageBroker;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.*;

@ViewScoped
@Named
@Slf4j
public class ViewNewProject {
    @Autowired
    private DSLContext funnyRanksDsl;

    @Getter
    private Project selectedProject;
    @Getter
    private List<Row<DriverProperty>> driverPropertyRows;

    @Getter
    private boolean connectionValidated;
    @Getter
    private boolean addDriverPropertyBtnDisabled;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        selectedProject = new Project();
        selectedProject.setRegDatetime(LocalDateTime.now());

        String defaultTimeZoneStr = TimeZone.getDefault().getID();
        ProjectDatabaseServerTimezone detectedTimeZone = timezoneEnumByLiteral
                .getOrDefault(defaultTimeZoneStr, null);
        selectedProject.setDatabaseServerTimezone(detectedTimeZone);

        driverPropertyRows = new ArrayList<>();
    }

    public void validateDatabaseConnection() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            addDriverPropertyBtnDisabled = false;
            return;
        }
        log.info("Attempting to validate database connection of project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        connectionValidated = false;
        FacesContext fc = FacesContext.getCurrentInstance();
        List<String> requiredTables = FUNNYRANKS_STATS.getTables()
                .stream()
                .map(org.jooq.Named::getName)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        List<String> tableNamesSlice;
        try (HikariDataSource hds = buildHikariDataSource(selectedProject)) {
            if (selectedProject.getDatabaseServerTimezone() != null)
                hds.addDataSourceProperty("serverTimezone", selectedProject.getDatabaseServerTimezone().getLiteral());

            for (Iterator<Row<DriverProperty>> iterator = driverPropertyRows.iterator(); iterator.hasNext(); ) {
                Row<DriverProperty> row = iterator.next();
                DriverProperty driverProperty = row.getPojo();
                PojoStatus pojoStatus = row.getStatus();
                if (pojoStatus == TO_REMOVE) {
                    iterator.remove();
                    continue;
                }
                if (isBlank(driverProperty.getKey())) {
                    iterator.remove();
                    continue;
                }
                if (isBlank(driverProperty.getValue()))
                    driverProperty.setValue("");
                if (log.isDebugEnabled())
                    log.debug("\naddDataSourceProperty " + driverProperty.getKey() + "=" + driverProperty.getValue());
                hds.addDataSourceProperty(driverProperty.getKey(), driverProperty.getValue());
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
                    "Failed validation new project", e.toString()));
            return;
        } finally {
            addDriverPropertyBtnDisabled = false;
        }
        if (!requiredTables.equals(tableNamesSlice)) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed validation new project",
                    "One of '" + selectedProject.getDatabaseSchema() + "' database tables is missing: "
                        + "required: " + requiredTables + ", founded: " + tableNamesSlice + ". "
                        + "You must manually import tables from *.sql files from https://github.com/mbto/funnyranks"));
            return;
        }
        fc.addMessage("msgs", new FacesMessage("Project settings validated", ""));
        connectionValidated = true;
    }

    public String saveDatabaseConnection() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            connectionValidated = false;
            addDriverPropertyBtnDisabled = false;
            return null;
        }
        log.info("Attempting to save database connection project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                log.info("Inserting project " + projectToString(selectedProject) + " " + getManagerInfo(funnyRanksManager));
                UInteger projectId = transactionalDsl.insertInto(PROJECT)
                        .set(PROJECT.NAME, selectedProject.getName())
                        .set(PROJECT.DESCRIPTION, isBlank(selectedProject.getDescription()) ? null : selectedProject.getDescription())
                        .set(PROJECT.DATABASE_HOSTPORT, selectedProject.getDatabaseHostport())
                        .set(PROJECT.DATABASE_SCHEMA, selectedProject.getDatabaseSchema())
                        .set(PROJECT.DATABASE_USERNAME, selectedProject.getDatabaseUsername())
                        .set(PROJECT.DATABASE_PASSWORD, selectedProject.getDatabasePassword())
                        .set(PROJECT.DATABASE_SERVER_TIMEZONE, selectedProject.getDatabaseServerTimezone())
                        .returning(PROJECT.ID)
                        .fetchOne().getId();
                ++localChangesCounter;
                selectedProject.setId(projectId);
                for (Row<DriverProperty> row : driverPropertyRows) {
                    DriverProperty driverProperty = row.getPojo();
                    driverProperty.setProjectId(projectId);
                    log.info("Inserting database property " + driverProperty + " for project " + projectToString(selectedProject)
                            + " " + getManagerInfo(funnyRanksManager));
                    localChangesCounter += transactionalDsl.insertInto(DRIVER_PROPERTY)
                            .set(DRIVER_PROPERTY.KEY, driverProperty.getKey())
                            .set(DRIVER_PROPERTY.VALUE, driverProperty.getValue())
                            .set(DRIVER_PROPERTY.PROJECT_ID, projectId)
                            .execute();
                }
            });
//            changesCounter.increment(localChangesCounter);
            return "/editProject?faces-redirect=true&projectId=" + selectedProject.getId();
        } catch (Throwable e) {
            selectedProject.setId(null);
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save new project", e.toString()));

            return null;
        } finally {
            connectionValidated = false;
            addDriverPropertyBtnDisabled = false;
        }
    }

    public void onRowEdit(RowEditEvent event) {
        connectionValidated = false;
        //noinspection unchecked
        Row<DriverProperty> row = (Row<DriverProperty>) event.getObject();

        if (driverPropertyRows.get(driverPropertyRows.size() - 1).equals(row)) {
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
        driverPropertyRows.add(new Row<>(driverProperty, NEW));
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