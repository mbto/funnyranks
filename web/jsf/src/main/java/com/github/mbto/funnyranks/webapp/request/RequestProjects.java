package com.github.mbto.funnyranks.webapp.request;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.webapp.DependentUtil;
import com.github.mbto.funnyranks.webapp.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;
import java.util.Set;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;

@RequestScoped
@Named
@Slf4j
public class RequestProjects {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Project> projects;

    @PostConstruct
    public void init() {
        if (log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc.isPostback()) {
            if (util.trySendRedirect("showProjectsForm", "projectsTblId", "editProject", "projectId"))
                return;
        }
    }

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = WebUtils.extractFunnyRanksManager(true, "fetchMsgs")) == null)
            return;
        Condition condition;
        if (funnyRanksManager.getRole() == ManagerRole.project) {
            Set<UInteger> availableProjectIds = funnyRanksManager.getAvailableProjectIds();
            if (availableProjectIds == null || availableProjectIds.isEmpty())
                return;
            condition = PROJECT.ID.in(availableProjectIds);
        } else
            condition = DSL.trueCondition();
        projects = funnyRanksDsl.selectFrom(PROJECT)
                .where(condition)
                .orderBy(PROJECT.REG_DATETIME.desc(), PROJECT.ID.desc())
                .fetchInto(Project.class);
    }
}