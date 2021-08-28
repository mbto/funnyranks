package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.webapp.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Set;

import static com.github.mbto.funnyranks.common.Constants.BCRYPT_PATTERN;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Manager.MANAGER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.pointwiseUpdateQuery;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

@ViewScoped
@Named
@Slf4j
public class ViewEditProfile {
    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;
    @Autowired
    private DSLContext funnyRanksDsl;

    @Getter
    private FunnyRanksManager selectedManager;
    @Getter
    private String projects;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        if((selectedManager = WebUtils.extractFunnyRanksManager(true, "fetchMsgs")) == null)
            return;
        if(selectedManager.canManageBroker())
            return;
        Set<UInteger> availableProjectIds = selectedManager.getAvailableProjectIds();
        if(availableProjectIds != null && !availableProjectIds.isEmpty()) {
            projects = funnyRanksDsl.select(
                    DSL.groupConcat(DSL.concat(PROJECT.NAME, DSL.val("["), PROJECT.ID, DSL.val("]")))
                            .orderBy(PROJECT.ID.asc())
                            .separator("<br/>")
                            .as("values")
                    ).from(PROJECT)
                    .where(PROJECT.ID.in(availableProjectIds))
                    .fetchOneInto(String.class);
        }
    }

    public void save() {
        log.info("Attempting to save profile " + selectedManager.toStringSimple() + " " + getManagerInfo(selectedManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            if (!BCRYPT_PATTERN.matcher(selectedManager.getPassword()).matches())
                selectedManager.setPassword(bcryptPasswordEncoder.encode(selectedManager.getPassword()));
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                localChangesCounter += pointwiseUpdateQuery(transactionalDsl, MANAGER.ID, selectedManager.getId(),
                        Arrays.asList(
                                Pair.of(MANAGER.PASSWORD, selectedManager.getPassword())
                        ));
            });
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("Manager " + selectedManager.toStringSimple() + " saved",
                    localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save broker " + selectedManager.toStringSimple(),
                    e.toString()));
        }
    }
}