package com.github.mbto.funnyranks.webapp.request;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Broker;
import com.github.mbto.funnyranks.webapp.DependentUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;

@RequestScoped
@Named
@Slf4j
public class RequestBrokers {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Broker> brokers;
    @Getter
    private Map<UInteger, Integer> portsAtAllBrokers;

    @PostConstruct
    public void init() {
        if (log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc.isPostback()) {
            if (util.trySendRedirect("showBrokersForm", "brokersTblId", "editBroker", "brokerId"))
                return;
        }
    }

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        brokers = funnyRanksDsl.selectFrom(BROKER)
                .orderBy(BROKER.REG_DATETIME.desc())
                .fetchInto(Broker.class);

        portsAtAllBrokers = funnyRanksDsl.select(BROKER.ID, DSL.countDistinct(PORT.ID))
                .from(BROKER)
                .leftJoin(PORT).on(BROKER.ID.eq(PORT.BROKER_ID))
                .groupBy(BROKER.ID)
                .fetchMap(BROKER.ID, DSL.count());
    }
}