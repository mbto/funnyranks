package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Broker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Named;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;

@Service
@Lazy(false)
@Slf4j
public class BrokerHolder {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DSLContext funnyRanksDsl;

    @Getter
    private UInteger currentBrokerId;
    @Getter
    private boolean isDevEnvironment;
    @Getter
    private boolean isTestEnvironment;

    private Map<UInteger, Broker> availableBrokers;
    private long nextRefreshAvailableBrokers;

    @PostConstruct
    public void init() {
        List<String> profiles = Arrays.asList(applicationContext
                .getEnvironment()
                .getActiveProfiles());
        isDevEnvironment = profiles.contains("dev");
        isTestEnvironment = profiles.contains("test");
        reload();
    }

    public void reload() {
        try {
            String brokerNamePropertyKey = "funnyranks.broker.name";
            String brokerNamePropertyValue = applicationContext.getEnvironment()
                    .getProperty(brokerNamePropertyKey, String.class);
            if (StringUtils.isBlank(brokerNamePropertyValue)) {
                throw new IllegalStateException("Empty property '" + brokerNamePropertyKey + "'");
            }
            final String brokerName = brokerNamePropertyValue.trim();
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                List<String> requiredTables = FUNNYRANKS.getTables()
                        .stream()
                        .map(Named::getName)
                        .collect(Collectors.toUnmodifiableList());

                Field<String> tableNameField = DSL.field("TABLE_NAME", String.class).lower();
                List<String> tableNamesSlice = transactionalDsl.select(tableNameField)
                        .from(DSL.table("information_schema.TABLES"))
                        .where(DSL.field("TABLE_SCHEMA").eq(FUNNYRANKS.getName()),
                                tableNameField.in(requiredTables))
                        .orderBy(tableNameField.asc())
                        .fetchInto(tableNameField.getType());
                if (!requiredTables.equals(tableNamesSlice)) {
                    throw new IllegalStateException("One of `" + FUNNYRANKS.getName() + "` database tables is missing: "
                            + "required: " + requiredTables + ", founded: " + tableNamesSlice + ". "
                            + "You must manually import tables from *.sql files from https://github.com/mbto/funnyranks");
                }

                Broker currentBroker = transactionalDsl.select(BROKER.ID)
                        .from(BROKER)
                        .where(BROKER.NAME.eq(brokerName))
                        .fetchOneInto(Broker.class);

                if (currentBroker != null) {
                    currentBrokerId = currentBroker.getId();
                } else {
                    currentBrokerId = transactionalDsl.insertInto(BROKER)
                            .set(BROKER.NAME, brokerName)
                            .set(BROKER.DESCRIPTION, "Auto-added")
                            .returning(BROKER.ID)
                            .fetchOne().getId();
                }
            });

            getAvailableBrokers(true);
        } catch (Throwable e) {
            log.warn("Shutdown funnyranks broker, due " + e, e);

            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
            return;
        }
    }

    public void setCurrentBrokerId(UInteger currentBrokerId) {
        if (isDevEnvironment) {
            this.currentBrokerId = currentBrokerId;
        }
    }

    public Map<UInteger, Broker> getAvailableBrokers() {
        return getAvailableBrokers(false);
    }

    public synchronized Map<UInteger, Broker> getAvailableBrokers(boolean needRefresh) {
        long now = System.currentTimeMillis() / 1000;

        if (needRefresh || nextRefreshAvailableBrokers == 0 || now >= nextRefreshAvailableBrokers) {
            availableBrokers = funnyRanksDsl.selectFrom(BROKER)
                    .fetchMap(BROKER.ID, Broker.class);

            nextRefreshAvailableBrokers = now + (60 * 10); /* +10 mins */
        }

        return availableBrokers;
    }
}