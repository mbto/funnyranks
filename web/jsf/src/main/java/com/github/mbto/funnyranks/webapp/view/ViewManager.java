package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.webapp.PojoStatus;
import com.github.mbto.funnyranks.webapp.Row;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.Constants.BCRYPT_PATTERN;
import static com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole.project;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Manager.MANAGER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.pointwiseUpdateQuery;
import static com.github.mbto.funnyranks.webapp.PojoStatus.*;
import static com.github.mbto.funnyranks.webapp.WebUtils.*;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ViewScoped
@Named
@Slf4j
public class ViewManager {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;

    @Getter
    private List<Row<FunnyRanksManager>> currentRows;
    @Getter
    private SelectItem[] projects;

    @Getter
    private boolean addServerBtnDisabled;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        fetchManagers();
    }

    private void fetchManagers() {
        funnyRanksDsl.transaction(config -> {
            DSLContext transactionalDslContext = DSL.using(config);
            currentRows =
                    transactionalDslContext.selectFrom(MANAGER)
                            .fetchInto(FunnyRanksManager.class)
                            .stream()
                            .map(funnyRanksManager -> {
                                funnyRanksManager.setupAdditionals();
                                return new Row<>(funnyRanksManager.getId(), funnyRanksManager, EXISTED);
                            }).collect(Collectors.toList());

            projects = transactionalDslContext.selectFrom(PROJECT)
                    .fetchInto(Project.class)
                    .stream()
                    .map(project -> new SelectItem(project.getId(), ProjectUtils.projectToString(project)))
                    .toArray(SelectItem[]::new);
        });
    }

    public void validateManagerUsername(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (isBlank(value))
            throw makeValidatorException(value, "");
        if (value.equalsIgnoreCase("anonymousUser"))
            throw makeValidatorException(value, "Restricted username");
        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");
        if (log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentRows.size=" + currentRows.size());
        for (int i = 0; i < currentRows.size(); i++) {
            if (i == rowIndexVar)
                continue;
            Row<FunnyRanksManager> row = currentRows.get(i);
            FunnyRanksManager manager = row.getPojo();
            if (value.equalsIgnoreCase(manager.getUsername()))
                throw makeValidatorException(value, "Username already exists");
        }
    }

    public void save() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            return;
        }
        log.info("Attempting to save managers " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            List<Object> toRemoveManagerIds = new ArrayList<>();
            for (Iterator<Row<FunnyRanksManager>> iterator = currentRows.iterator(); iterator.hasNext(); ) {
                Row<FunnyRanksManager> row = iterator.next();
                FunnyRanksManager manager = row.getPojo();
                if (row.getStatus() == TO_REMOVE) {
                    if (row.getPrimaryKey() != null) {
                        toRemoveManagerIds.add(row.getPrimaryKey());
                        log.info("Deleting manager " + manager.toStringSimple() + " " + getManagerInfo(funnyRanksManager));
                    }
                    iterator.remove();
                } else if (row.getStatus() == NEW) {
                    if (isBlank(manager.getUsername()) || isBlank(manager.getPassword()) || manager.getRole() == null) {
                        iterator.remove();
                    }
                }
            }
            if (!toRemoveManagerIds.isEmpty() || !currentRows.isEmpty()) {
                funnyRanksDsl.transaction(config -> {
                    DSLContext transactionalDsl = DSL.using(config);
                    if (!toRemoveManagerIds.isEmpty()) {
                        localChangesCounter += transactionalDsl.deleteFrom(MANAGER)
                                .where(MANAGER.ID.in(toRemoveManagerIds))
                                .execute();
                    }

                    for (Row<FunnyRanksManager> row : currentRows) {
                        FunnyRanksManager manager = row.getPojo();
                        PojoStatus pojoStatus = row.getStatus();
                        if (pojoStatus == NEW || pojoStatus == CHANGED) {
                            if (!BCRYPT_PATTERN.matcher(manager.getPassword()).matches())
                                manager.setPassword(bcryptPasswordEncoder.encode(manager.getPassword()));
                        }
                        if (pojoStatus == CHANGED) {
                            manager.applyFromAdditionals();
                            log.info("Updating manager " + manager.toStringSimple() + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += pointwiseUpdateQuery(transactionalDsl, MANAGER.ID, row.getPrimaryKey(),
                                    Arrays.asList(
                                            Pair.of(MANAGER.USERNAME, manager.getUsername()),
                                            Pair.of(MANAGER.PASSWORD, manager.getPassword()),
                                            Pair.of(MANAGER.DESCRIPTION, isBlank(manager.getDescription()) ? null : manager.getDescription()),
                                            Pair.of(MANAGER.ROLE, manager.getRole()),
                                            Pair.of(MANAGER.ACTIVE, manager.getActive()),
                                            Pair.of(MANAGER.PROJECT_IDS, manager.canManageBroker() ? null : (manager.getProjectIds() != null ?
                                                    DSL.field("cast('" + manager.getProjectIds() + "' as json)") : null)))
                            );
                        } else if (pojoStatus == NEW) {
                            manager.applyFromAdditionals();
                            log.info("Inserting manager " + manager.toStringSimple() + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += transactionalDsl.insertInto(MANAGER)
                                    .set(MANAGER.USERNAME, manager.getUsername())
                                    .set(MANAGER.PASSWORD, bcryptPasswordEncoder.encode(manager.getPassword()))
                                    .set(MANAGER.DESCRIPTION, isBlank(manager.getDescription()) ? null : manager.getDescription())
                                    .set(MANAGER.ROLE, manager.getRole())
                                    .set(MANAGER.ACTIVE, manager.getActive())
                                    .set(MANAGER.PROJECT_IDS, manager.canManageBroker() ? null : manager.getProjectIds())
                                    .set(MANAGER.REG_DATETIME, manager.getRegDatetime())
                                    .execute();
                        }
                    }
                });
            }
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("Managers saved", localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save managers",
                    e.toString()));
        } finally {
            addServerBtnDisabled = false;
            fetchManagers();
        }
    }

    public void onRowEdit(RowEditEvent event) {
        //noinspection unchecked
        Row<FunnyRanksManager> row = (Row<FunnyRanksManager>) event.getObject();
        if (row.getPrimaryKey() != null) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if (currentRows.get(currentRows.size() - 1).equals(row)) {
            addServerBtnDisabled = false;
        }
        if (log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddManager() {
        if (log.isDebugEnabled())
            log.debug("\nonAddManager");

        FunnyRanksManager manager = new FunnyRanksManager();
        manager.setRole(project);
        manager.setRegDatetime(LocalDateTime.now());
        manager.setupAdditionals();
        currentRows.add(new Row<>(manager, NEW));
        addServerBtnDisabled = true;
    }

    public void onRestoreRow(Row<FunnyRanksManager> row) {
        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if (log.isDebugEnabled())
            log.debug("\nonRestoreRow " + row);
    }

    public void onRemoveRow(Row<FunnyRanksManager> row) {
        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if (log.isDebugEnabled())
            log.debug("\nonRemoveRow " + row);
    }
}