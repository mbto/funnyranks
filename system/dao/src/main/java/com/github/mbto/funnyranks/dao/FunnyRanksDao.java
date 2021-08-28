package com.github.mbto.funnyranks.dao;

import com.github.mbto.funnyranks.common.dto.FunnyRanksData;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.pojos.Country;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Game.GAME;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState.MAXMIND_DB_STATE;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.FunnyranksMaxmindCountry.FUNNYRANKS_MAXMIND_COUNTRY;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Country.COUNTRY;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Ipv4.IPV4;

@Repository
@Slf4j
public class FunnyRanksDao {
    @Autowired
    private DSLContext funnyRanksDsl;

    public FunnyRanksData fetchFunnyRanksData(UInteger brokerId, UInteger projectId, Set<UInteger> implementedAppIds) {
        return funnyRanksDsl.transactionResult(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            Condition brokerIdCondition = PORT.BROKER_ID.eq(brokerId);
            Condition projectIdCondition = projectId != null ? PORT.PROJECT_ID.eq(projectId) : DSL.trueCondition();

            Map<UInteger, Game> gameByAppId = transactionalDsl
                    .select(GAME.asterisk())
                    .from(GAME)
                    .join(PORT).on(GAME.APP_ID.eq(PORT.GAME_APP_ID))
                    .where(brokerIdCondition, projectIdCondition, GAME.APP_ID.in(implementedAppIds))
                    .groupBy(GAME.APP_ID)
                    .fetchMap(GAME.APP_ID, Game.class);

            Condition gameIdCondition = PORT.GAME_APP_ID.in(gameByAppId.keySet());

            List<Port> ports = transactionalDsl
                    .select(DSL.asterisk())
                    .from(PORT)
                    .where(brokerIdCondition, projectIdCondition, gameIdCondition)
                    .orderBy(PORT.PROJECT_ID.desc(), PORT.ID.asc())
                    .fetchInto(Port.class);

            Map<UInteger, Project> projectByProjectId = transactionalDsl
                    .select(PROJECT.asterisk())
                    .from(PROJECT)
                    .join(PORT).on(PROJECT.ID.eq(PORT.PROJECT_ID))
                    .where(brokerIdCondition, projectIdCondition, gameIdCondition)
                    .groupBy(PROJECT.ID)
                    .fetchMap(PROJECT.ID, Project.class);

            Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId = transactionalDsl
                    .select(DRIVER_PROPERTY.asterisk())
                    .from(DRIVER_PROPERTY)
                    .join(PORT).on(DRIVER_PROPERTY.PROJECT_ID.eq(PORT.PROJECT_ID))
                    .where(brokerIdCondition, projectIdCondition, gameIdCondition)
                    .groupBy(DRIVER_PROPERTY.ID)
                    .fetchGroups(DRIVER_PROPERTY.PROJECT_ID, DriverProperty.class);

            FunnyRanksData funnyRanksData = new FunnyRanksData();
            funnyRanksData.setGameByAppId(gameByAppId);
            funnyRanksData.setPorts(ports);
            funnyRanksData.setProjectByProjectId(projectByProjectId);
            funnyRanksData.setDriverPropertiesByProjectId(driverPropertiesByProjectId);
            return funnyRanksData;
        });
    }

    public Record2<Integer, Integer> fetchPortsCountByAliasRecord(UInteger projectId,
                                                                  UInteger currentBrokerId) {
        return funnyRanksDsl.select(
                DSL.selectCount()
                        .from(PORT)
                        .join(BROKER).on(PORT.BROKER_ID.eq(BROKER.ID))
                        .where(PORT.PROJECT_ID.eq(projectId),
                                BROKER.ID.eq(currentBrokerId))
                        .<Integer>asField("at_broker"),
                DSL.selectCount()
                        .from(PORT)
                        .where(PORT.PROJECT_ID.eq(projectId))
                        .<Integer>asField("at_all_brokers")
        ).fetchOne();
    }

    public String fetchMaxmindDbStateComment() {
        Field<String> tableNameField = DSL.field("TABLE_COMMENT", String.class).trim();
        return funnyRanksDsl.select(tableNameField)
                .from(DSL.table("information_schema.TABLES"))
                .where(DSL.field("TABLE_SCHEMA").eq(FUNNYRANKS.getName()),
                        DSL.field("TABLE_NAME").eq(MAXMIND_DB_STATE.getName()),
                        tableNameField.isNotNull(),
                        tableNameField.notEqual(""))
                .fetchOneInto(tableNameField.getType());
    }

    /**
     * WITH `cte` AS (
     * ( SELECT 1431655764 AS `n` FROM DUAL ) UNION ALL
     * ( SELECT 1073741808 AS `n` FROM DUAL )) SELECT
     * `cte`.`n`, `country`.`emoji`, `country`.`name_ru`
     * FROM `cte` JOIN `ipv4` ON `cte`.`n` BETWEEN `ipv4`.`start_int` AND `ipv4`.`last_int`
     * JOIN `country` ON `country`.`geoname_id` = `ipv4`.`v_geoname_id`
     */
    public void fillSessionByIdentityContainerWithCounties(PortData portData,
                                                           Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity) {
        String ipAlias = "n";
        SelectSelectStep<Record1<UInteger>> unionIps = archivedSessionViewsByIdentity.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(archivedSessionView -> !archivedSessionView.getArchivedSession().isCountrySetted()
                        && archivedSessionView.getArchivedSession().getIp() != null)
                .map(archivedSessionView -> archivedSessionView.getArchivedSession().getIp())
                .distinct()
                .map(value -> DSL.select(DSL.val(value).as(ipAlias)))
                .reduce((r1, r2) -> (SelectSelectStep<Record1<UInteger>>) r1.unionAll(r2))
                .orElse(null);
        if (unionIps == null)
            return;
        log.info("Fetching data from MaxMind GeoLite2 country database");
        CommonTableExpression<Record1<UInteger>> cte = DSL.name("cte").as(unionIps);
        Field<UInteger> ipField = cte.field(ipAlias, UInteger.class);
        Map<UInteger, Country> countryByIp;
        try {
            countryByIp = funnyRanksDsl.transactionResult(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                try {
                    transactionalDsl.execute("LOCK TABLES " + // sync if updating by GeoLite2UpdaterService
                            String.join(", ",
                                    "`" + FUNNYRANKS.getName() + "`.`" + MAXMIND_DB_STATE.getName() + "` READ",
                                    "`" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "`.`" + IPV4.getName() + "` READ",
                                    "`" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "`.`" + COUNTRY.getName() + "` READ"
                            )
                    );
                    List<String> requiredTables = FUNNYRANKS_MAXMIND_COUNTRY.getTables()
                            .stream()
                            .map(Named::getName)
                            .collect(Collectors.toUnmodifiableList());
                    int tablesCount = transactionalDsl
                            .selectCount()
                            .from(DSL.table("information_schema.TABLES"))
                            .where(DSL.field("TABLE_SCHEMA").eq(FUNNYRANKS_MAXMIND_COUNTRY.getName()),
                                    DSL.field("TABLE_NAME", String.class).in(requiredTables)
                            ).fetchOneInto(int.class);
                    if (tablesCount != requiredTables.size()) {
                        log.info("Skip fetching data from MaxMind GeoLite2 country database, due some tables " + requiredTables + " not exists");
                        return null;
                    }
                    return transactionalDsl
                            .with(cte)
                            .select(ipField,
                                    COUNTRY.EMOJI,
                                    COUNTRY.field("name_" + portData.getProject().getLanguage().getLiteral(), String.class))
                            .from(cte)
                            .join(IPV4).on(ipField.between(IPV4.START_INT, IPV4.LAST_INT))
                            .join(COUNTRY).on(COUNTRY.GEONAME_ID.eq(IPV4.V_GEONAME_ID))
                            .fetchMap(ipField, Country.class);
                } finally {
                    try {
                        transactionalDsl.execute("UNLOCK TABLES");
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable e) {
            log.warn("Failed fetching data from MaxMind GeoLite2 country database", e);
            return;
        }
        if (!countryByIp.isEmpty()) {
            archivedSessionViewsByIdentity.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(ags -> !ags.getArchivedSession().isCountrySetted())
                    .forEach(archivedSessionView -> {
                        Session session = archivedSessionView.getArchivedSession();
                        UInteger ip = session.getIp();
                        if (ip != null) {
                            Country country = countryByIp.get(ip);
                            if (country != null) {
                                String countryName;
                                switch (portData.getProject().getLanguage()) {
                                    case ru:
                                        countryName = country.getNameRu();
                                        break;
                                    default:
                                        countryName = country.getNameEn();
                                }
                                session.setCountryData(country.getEmoji(), countryName);
                            } else
                                session.setCountrySetted(true);
                        } else
                            session.setCountrySetted(true);
                    });
        }
    }
}