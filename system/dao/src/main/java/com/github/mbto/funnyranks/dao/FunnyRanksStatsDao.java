package com.github.mbto.funnyranks.dao;

import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbyip;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbyname;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.routines.Playerbysteamid64;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records.PlayerIpRecord;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records.PlayerNameRecord;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records.PlayerRecord;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records.PlayerSteamidRecord;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats.FUNNYRANKS_STATS;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.Tables.RANK;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.History.HISTORY;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.Player.PLAYER;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerIp.PLAYER_IP;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerName.PLAYER_NAME;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerSteamid.PLAYER_STEAMID;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.buildHikariDataSource;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.configurateJooqContext;

@Repository
@Slf4j
public class FunnyRanksStatsDao {
    private static final Supplier<Set<Object>> plannedDataContainerSupplier = LinkedHashSet::new;

    public void mergeIdentities(PortData portData,
                                Map<Identity, List<ArchivedSessionView>> archivedSessionViewsByIdentity) {
        if (log.isDebugEnabled())
            log.debug("mergeIdentities() start");

        Project project = portData.getProject();
        ProjectMergeType mergeType = project.getMergeType();
        String languageLiteral = project.getLanguage().getLiteral();
        try (HikariDataSource hds = buildHikariDataSource(project, portData.convertDriverPropertiesToProperties())) {
            if (project.getDatabaseServerTimezone() != null)
                hds.addDataSourceProperty("serverTimezone", project.getDatabaseServerTimezone().getLiteral());
            log.info(ProjectUtils.hikariDataSourceToString(hds));
            DSLContext funnyRanksStatsDsl = configurateJooqContext(hds, 15, FUNNYRANKS_STATS.getName(), project.getDatabaseSchema());
            funnyRanksStatsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                try {
                    transactionalDsl.execute("LOCK TABLES " +
                            String.join(", ",
                                    "`" + project.getDatabaseSchema() + "`.`" + HISTORY.getName() + "` WRITE",
                                    "`" + project.getDatabaseSchema() + "`.`" + PLAYER.getName() + "` WRITE",
                                    "`" + project.getDatabaseSchema() + "`.`" + PLAYER_NAME.getName() + "` WRITE",
                                    "`" + project.getDatabaseSchema() + "`.`" + PLAYER_IP.getName() + "` WRITE",
                                    "`" + project.getDatabaseSchema() + "`.`" + PLAYER_STEAMID.getName() + "` WRITE",
                                    "`" + project.getDatabaseSchema() + "`.`" + RANK.getName() + "` WRITE"
                            )
                    );

                    for (Map.Entry<Identity, List<ArchivedSessionView>> entry : archivedSessionViewsByIdentity.entrySet()) {
                        Identity identity = entry.getKey();
                        List<ArchivedSessionView> archivedSessionViews = entry.getValue();
                        UInteger playerId = null;
                        if (mergeType == ProjectMergeType.Nick) {
// select * from player p join player_name pn on p.id = pn.player_id where pn.`name` = '222' order by pn.reg_datetime desc limit 1;
                            Playerbyname fetchPlayerRoutine = new Playerbyname();
                            fetchPlayerRoutine.setName_((String) identity.getPojo());
                            fetchPlayerRoutine.setRanksTotal(null);
                            fetchPlayerRoutine.setLang(languageLiteral);
                            fetchPlayerRoutine.execute(transactionalDsl.configuration());
                            List<ResultOrRows> resultOrRows = fetchPlayerRoutine.getResults().resultsOrRows();
                            Result<Record> result = resultOrRows.get(0).result();
                            if (result.isNotEmpty())
                                playerId = result.getValue(0, PLAYER.ID);
                        } else if (mergeType == ProjectMergeType.IP) {
                            Playerbyip fetchPlayerRoutine = new Playerbyip();
                            fetchPlayerRoutine.setIp((UInteger) identity.getPojo());
                            fetchPlayerRoutine.setRanksTotal(null);
                            fetchPlayerRoutine.setLang(languageLiteral);
                            fetchPlayerRoutine.execute(transactionalDsl.configuration());
                            List<ResultOrRows> resultOrRows = fetchPlayerRoutine.getResults().resultsOrRows();
                            Result<Record> result = resultOrRows.get(0).result();
                            if (result.isNotEmpty())
                                playerId = result.getValue(0, PLAYER.ID);
                        } else if (mergeType == ProjectMergeType.Steam_ID) {
                            Playerbysteamid64 fetchPlayerRoutine = new Playerbysteamid64();
                            fetchPlayerRoutine.setSteamid64((Long) identity.getPojo());
                            fetchPlayerRoutine.setRanksTotal(null);
                            fetchPlayerRoutine.setLang(languageLiteral);
                            fetchPlayerRoutine.execute(transactionalDsl.configuration());
                            List<ResultOrRows> resultOrRows = fetchPlayerRoutine.getResults().resultsOrRows();
                            Result<Record> result = resultOrRows.get(0).result();
                            if (result.isNotEmpty())
                                playerId = result.getValue(0, PLAYER.ID);
                        } else
                            throw new UnsupportedOperationException("Unsupported mergeType '" + mergeType + "'");
                        long totalKills = 0;
                        long totalDeaths = 0;
                        long totalGamingTimeSecsSecs = 0;
                        LocalDateTime lastSeenDateTime = archivedSessionViews.stream()
                                .map(archivedSessionView -> archivedSessionView.getArchivedSession().getFinished())
                                .max(LocalDateTime::compareTo)
                                .orElse(portData.getLastTouchDateTime());
                        Set<Object> plannedNames = plannedDataContainerSupplier.get();
                        Set<Object> plannedIps = null;
                        Set<Object> plannedSteamIds64 = null;
                        Map<UInteger, String[]> countryDataByIp = null;
                        for (ArchivedSessionView archivedSessionView : archivedSessionViews) {
                            Session archivedSession = archivedSessionView.getArchivedSession();
                            totalKills += archivedSession.getKills();
                            totalDeaths += archivedSession.getDeaths();
                            totalGamingTimeSecsSecs += archivedSession.calcGamingTimeSecs();
                            plannedNames.add(archivedSessionView.getName());
                            if (archivedSession.getIp() != null) {
                                if (plannedIps == null)
                                    plannedIps = plannedDataContainerSupplier.get();
                                plannedIps.add(archivedSession.getIp());
                                if (countryDataByIp == null)
                                    countryDataByIp = new HashMap<>();
                                countryDataByIp.put(archivedSession.getIp(), new String[]{
                                        archivedSession.getCountryEmoji(),
                                        archivedSession.getCountryName()});
                            }
                            if (archivedSession.getSteamId64() != null) {
                                if (plannedSteamIds64 == null)
                                    plannedSteamIds64 = plannedDataContainerSupplier.get();
                                plannedSteamIds64.add(archivedSession.getSteamId64());
                            }
                        }
                        Port port = portData.getPort();
                        if (playerId == null) {
                            playerId = transactionalDsl.insertInto(PLAYER)
                                    .set(PLAYER.KILLS, UInteger.valueOf(totalKills))
                                    .set(PLAYER.DEATHS, UInteger.valueOf(totalDeaths))
                                    .set(PLAYER.TIME_SECS, UInteger.valueOf(totalGamingTimeSecsSecs))
                                    .set(PLAYER.LASTSEEN_DATETIME, lastSeenDateTime)
                                    .set(PLAYER.LAST_SERVER_NAME, port.getName())
                                    .returning(PLAYER.ID)
                                    .fetchOne().getId();
                        } else {
                            UpdateSetFirstStep<PlayerRecord> updateStep = transactionalDsl.update(PLAYER);
                            if (totalKills != 0)
                                updateStep.set(PLAYER.KILLS, PLAYER.KILLS.plus(UInteger.valueOf(totalKills)));
                            if (totalDeaths != 0)
                                updateStep.set(PLAYER.DEATHS, PLAYER.DEATHS.plus(UInteger.valueOf(totalDeaths)));

                            updateStep.set(PLAYER.TIME_SECS, PLAYER.TIME_SECS.plus(UInteger.valueOf(totalGamingTimeSecsSecs)))
                                    .set(PLAYER.LASTSEEN_DATETIME, lastSeenDateTime)
                                    .set(PLAYER.LAST_SERVER_NAME, port.getName())
                                    .where(PLAYER.ID.eq(playerId))
                                    .execute();
                        }

                        mergeRelations(transactionalDsl, playerId, lastSeenDateTime,
                                plannedNames, PlayerNameRecord::new,
                                null,
                                PLAYER_NAME.ID, PLAYER_NAME.PLAYER_ID, PLAYER_NAME.NAME, PLAYER_NAME.REG_DATETIME);

                        mergeRelations(transactionalDsl, playerId, lastSeenDateTime,
                                plannedIps, PlayerIpRecord::new,
                                countryDataByIp,
                                PLAYER_IP.ID, PLAYER_IP.PLAYER_ID, PLAYER_IP.IP, PLAYER_IP.REG_DATETIME);

                        mergeRelations(transactionalDsl, playerId, lastSeenDateTime,
                                plannedSteamIds64, PlayerSteamidRecord::new,
                                null,
                                PLAYER_STEAMID.ID, PLAYER_STEAMID.PLAYER_ID, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.REG_DATETIME);
                    }
                } finally {
                    try {
                        transactionalDsl.execute("UNLOCK TABLES");
                    } catch (Throwable ignored) {
                    }
                }
            });
        }

        if (log.isDebugEnabled())
            log.debug("mergeIdentities() end");
    }

    @SuppressWarnings({"rawtypes", "unchecked", "SuspiciousMethodCalls"})
    private void mergeRelations(DSLContext transactionalDsl,
                                UInteger playerId,
                                LocalDateTime lastSeenDateTime,
                                Set<Object> plannedDatas,
                                Supplier<UpdatableRecordImpl<?>> recordSupplier,
                                Map<UInteger, String[]> countryDataByIp,
                                TableField<?, UInteger> pkField,
                                TableField<?, UInteger> fkField,
                                TableField<?, ?> dataField,
                                TableField<?, LocalDateTime> regDatetimeField) {
        Table<?> targetTable = pkField.getTable();
        if (plannedDatas != null) {
            SelectConditionStep<? extends Record1<?>> selectConditionStep = transactionalDsl.select(dataField)
                    .from(targetTable)
                    .where(fkField.eq(playerId), dataField.in(plannedDatas));

            if (dataField.getType() == String.class) { // names
                List<?> existedDatas = selectConditionStep.fetch(dataField);
                plannedDatas.removeIf(plannedData -> {
                    for (Object existedData : existedDatas) {
                        /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
                        //noinspection StringOperationCanBeSimplified
                        if (((String) plannedData).toLowerCase().equals(((String) existedData).toLowerCase()))
                            return true;
                    }
                    return false;
                });
            } else { // ips/steamIds
                Set<?> existedDatas = selectConditionStep.fetchSet(dataField);
                plannedDatas.removeIf(existedDatas::contains);
            }
            if (!plannedDatas.isEmpty()) {
                List<? extends UpdatableRecordImpl<?>> records = plannedDatas.stream()
                        .map(plannedData -> {
                            UpdatableRecordImpl<?> record = recordSupplier.get();
                            record.set(fkField, playerId);
                            record.set((Field) dataField, dataField.getType().cast(plannedData));
                            record.set(regDatetimeField, lastSeenDateTime);
                            if (countryDataByIp != null && !countryDataByIp.isEmpty()) {
                                String[] countryData = countryDataByIp.get(plannedData);
                                if (countryData != null) {
                                    record.set(PLAYER_IP.COUNTRY_EMOJI, countryData[0]);
                                    record.set(PLAYER_IP.COUNTRY_NAME, countryData[1]);
                                }
                            }
                            return record;
                        }).collect(Collectors.toList());
                transactionalDsl.batchInsert(records).execute();
            }
        }
        int dataCount = transactionalDsl.fetchCount(targetTable, fkField.eq(playerId));
        if (dataCount > 15) {
            SelectLimitPercentStep<?> subquery = DSL.select(pkField)
                    .from(targetTable)
                    .where(fkField.eq(playerId))
                    .orderBy(regDatetimeField.asc())
                    .limit(dataCount - 15);

            transactionalDsl.deleteFrom(targetTable)
                    .where(fkField.eq(playerId),
                            pkField.in(DSL.select(subquery.field(pkField)).from(subquery))
                    ).execute();
        }
    }
}