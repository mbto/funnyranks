package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Broker;
import com.github.mbto.funnyranks.service.BrokerHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static com.github.mbto.funnyranks.webapp.WebUtils.canManageBroker;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ViewScoped
@Named
@Slf4j
public class ViewEditBroker {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private BrokerHolder brokerHolder;

    @Getter
    private Broker selectedBroker;

    @Getter
    private int portsAtAllBrokers;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        if (canManageBroker(true, "fetchMsgs") == null)
            return;
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String brokerIdStr = request.getParameter("brokerId");
        UInteger brokerId;
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            brokerId = UInteger.valueOf(brokerIdStr);
        } catch (Throwable e) {
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid brokerId", ""));
            return;
        }
        selectedBroker = brokerHolder.getAvailableBrokers(true).get(brokerId);
        if (selectedBroker == null) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Broker[" + brokerId + "] not founded", ""));
            return;
        }
        fetchPortsCounts();
    }

    private void fetchPortsCounts() {
        portsAtAllBrokers = funnyRanksDsl.selectCount()
                .from(PORT)
                .where(PORT.BROKER_ID.eq(selectedBroker.getId()))
                .fetchOne(DSL.count());
    }

    public void save() {
        log.info("Attempting to save broker " + brokerToString(selectedBroker) + " " + getManagerInfo());
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                localChangesCounter += pointwiseUpdateQuery(transactionalDsl, BROKER.ID, selectedBroker.getId(),
                        Arrays.asList(
                                Pair.of(BROKER.DESCRIPTION, isBlank(selectedBroker.getDescription()) ? null : selectedBroker.getDescription())
                        ));
            });
//            changesCounter.increment(localChangesCounter);
            brokerHolder.getAvailableBrokers(true);
            fc.addMessage("msgs", new FacesMessage("Broker " + brokerToString(selectedBroker) + " saved",
                    localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save broker " + brokerToString(selectedBroker),
                    e.toString()));
        }
    }

    public String delete() {
        log.info("Attempting to delete broker " + brokerToString(selectedBroker) + " " + getManagerInfo());
        localChangesCounter = 0;
        try {
            fetchPortsCounts();
            if (portsAtAllBrokers > 0) {
                throw new IllegalStateException("You must delete "
                        + declension2(portsAtAllBrokers, "port") + " from all brokers for delete this broker");
            }
            localChangesCounter += funnyRanksDsl.deleteFrom(BROKER)
                    .where(BROKER.ID.eq(selectedBroker.getId()))
                    .execute();
//            changesCounter.increment(localChangesCounter);
            brokerHolder.getAvailableBrokers(true);
        } catch (Throwable e) {
            FacesContext.getCurrentInstance().addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed delete broker " + brokerToString(selectedBroker),
                    e.toString()));
            return null;
        }
        return "/brokers?faces-redirect=true";
    }

    public void shutdownBroker() {
        FunnyRanksManager funnyRanksManager;
        if ((funnyRanksManager = canManageBroker()) == null)
            return;
        log.info("Shutdown received from frontend " + getManagerInfo(funnyRanksManager));
        int code = SpringApplication.exit(applicationContext, () -> 1);
        System.exit(code);
    }
}