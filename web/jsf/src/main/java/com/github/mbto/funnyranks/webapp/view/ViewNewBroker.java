package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Broker;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.records.BrokerRecord;
import com.github.mbto.funnyranks.service.BrokerHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.brokerToString;
import static com.github.mbto.funnyranks.webapp.WebUtils.canManageBroker;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ViewScoped
@Named
@Slf4j
public class ViewNewBroker {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private BrokerHolder brokerHolder;

    @Getter
    private Broker selectedBroker;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        selectedBroker = new Broker();
        selectedBroker.setRegDatetime(LocalDateTime.now());
    }

    public String save() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            return null;
        }
        log.info("Attempting to save broker " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                log.info("Inserting broker " + brokerToString(selectedBroker) + " " + getManagerInfo(funnyRanksManager));
                BrokerRecord brokerRecord = transactionalDsl.insertInto(BROKER)
                        .set(BROKER.NAME, selectedBroker.getName())
                        .set(BROKER.DESCRIPTION, isBlank(selectedBroker.getDescription()) ? null : selectedBroker.getDescription())
                        .returning(BROKER.asterisk())
                        .fetchOne();
                ++localChangesCounter;
                selectedBroker = brokerRecord.into(Broker.class);
            });
//            changesCounter.increment(localChangesCounter);
            brokerHolder.getAvailableBrokers(true);
            return "/editBroker?faces-redirect=true&brokerId=" + selectedBroker.getId();
        } catch (Throwable e) {
            selectedBroker.setId(null);
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save new broker", e.toString()));

            return null;
        }
    }

    public void validateBrokerName(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (brokerHolder.getAvailableBrokers()
                .values()
                .stream()
                .anyMatch(broker -> value.equalsIgnoreCase(broker.getName())))
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Broker name '" + value + "' already existed", ""));
    }
}