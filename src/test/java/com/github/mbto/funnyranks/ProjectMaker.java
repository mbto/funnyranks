package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.Player;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerIp;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerName;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerSteamid;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats.FUNNYRANKS_STATS;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.Tables.HISTORY;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.Tables.PLAYER_NAME;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.Player.PLAYER;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerIp.PLAYER_IP;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerSteamid.PLAYER_STEAMID;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;

@Component
@Profile("test")
@Slf4j
public class ProjectMaker {
    @Autowired
    private ThreadPoolTaskExecutor senderTE;

    @Getter
    private List<Player> players;
    @Getter
    private List<PlayerIp> playersIps;
    @Getter
    private List<PlayerName> playerNames;
    @Getter
    private List<PlayerSteamid> playerSteamIds;

    private List<TableField<?, ?>> excludeColumns;

    public void process(Project project, Runnable job) {
        process(project, job, null);
    }

    public void process(Project project, Runnable job, List<TableField<?, ?>> excludeColumns) {
        this.excludeColumns = excludeColumns;
//        try (HikariDataSource hds = buildHikariDataSource(project, null, "TRANSACTION_SERIALIZABLE")) {
        try (HikariDataSource hds = buildHikariDataSource(project, null, "TRANSACTION_REPEATABLE_READ")) {
            if (project.getDatabaseServerTimezone() != null)
                hds.addDataSourceProperty("serverTimezone", project.getDatabaseServerTimezone().getLiteral());
            log.info(hikariDataSourceToString(hds));
            DSLContext funnyRanksStatsDsl = configurateJooqContext(hds, 10, FUNNYRANKS_STATS.getName(), project.getDatabaseSchema());
            try {
                funnyRanksStatsDsl.execute("SET FOREIGN_KEY_CHECKS = 0");
                funnyRanksStatsDsl.truncate(HISTORY).execute();
                funnyRanksStatsDsl.truncate(PLAYER).execute();
                funnyRanksStatsDsl.truncate(PLAYER_IP).execute();
                funnyRanksStatsDsl.truncate(PLAYER_NAME).execute();
                funnyRanksStatsDsl.truncate(PLAYER_STEAMID).execute();
            } finally {
                try {
                    funnyRanksStatsDsl.execute("SET FOREIGN_KEY_CHECKS = 1");
                } catch (Throwable ignored) {}
            }
            job.run();
            while (true) {
                int queueSize = senderTE.getThreadPoolExecutor().getQueue().size();
                int activeCount = senderTE.getActiveCount();
                if (queueSize == 0 && activeCount == 0) {
                    break;
                }
                log.debug("Waiting empty queue, queueSize=" + queueSize + ", activeCount=" + activeCount);
                try {
                    //noinspection BusyWait
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (Throwable ignored) {}
            }
            funnyRanksStatsDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);
                players = transactionalDsl.select(buildRequestedFields(PLAYER))
                        .from(PLAYER)
                        .orderBy(PLAYER.TIME_SECS.desc(),
                                PLAYER.KILLS.desc(),
                                PLAYER.DEATHS.desc()
                        ).fetchInto(Player.class);
                playersIps = transactionalDsl.select(buildRequestedFields(PLAYER_IP))
                        .from(PLAYER_IP)
                        .orderBy(PLAYER_IP.ID.asc())
                        .fetchInto(PlayerIp.class);
                playerNames = transactionalDsl.select(buildRequestedFields(PLAYER_NAME))
                        .from(PLAYER_NAME)
                        .orderBy(PLAYER_NAME.ID.asc())
                        .fetchInto(PlayerName.class);
                playerSteamIds = transactionalDsl.select(buildRequestedFields(PLAYER_STEAMID))
                        .from(PLAYER_STEAMID)
                        .orderBy(PLAYER_STEAMID.ID.asc())
                        .fetchInto(PlayerSteamid.class);
            });
        }

        System.out.println("\nActual players:");
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            System.out.println("{"
                            + String.join(", ",
                            quote(player.getId()),
                            quote(player.getKills()),
                            quote(player.getDeaths()),
                            quote(player.getTimeSecs()),
                            quote(player.getStarsUnicode()),
                            quote(player.getStarsCompat()),
                            quote(player.getLevel()),
                            quote(player.getLastseenDatetime(), YYYYMMDD_HHMMSS_PATTERN),
                            quote(player.getLastServerName())
                    ) + "}" + (i + 1 < players.size() ? "," : "")
                            + " // " + humanLifetime(player.getTimeSecs().longValue() * 1000)
            );
        }
        System.out.println("\nActual players ips:");
        for (int i = 0; i < playersIps.size(); i++) {
            PlayerIp pojo = playersIps.get(i);
            System.out.println("{"
                            + String.join(", ", quote(pojo.getId()),
                            quote(pojo.getPlayerId()),
                            quote(pojo.getIp4()),
                            quote(pojo.getRegDatetime(), YYYYMMDD_HHMMSS_PATTERN)
                    ) + "}" + (i + 1 < playersIps.size() ? "," : "")
            );
        }
        System.out.println("\nActual players names:");
        for (int i = 0; i < playerNames.size(); i++) {
            PlayerName pojo = playerNames.get(i);
            System.out.println("{"
                            + String.join(", ", quote(pojo.getId()),
                            quote(pojo.getPlayerId()),
                            quote(pojo.getName()),
                            quote(pojo.getRegDatetime(), YYYYMMDD_HHMMSS_PATTERN)
                    ) + "}" + (i + 1 < playerNames.size() ? "," : "")
            );
        }

        System.out.println("\nActual players steamIds:");
        for (int i = 0; i < playerSteamIds.size(); i++) {
            PlayerSteamid pojo = playerSteamIds.get(i);
            System.out.println("{"
                            + String.join(", ", quote(pojo.getId()),
                            quote(pojo.getPlayerId()),
                            quote(pojo.getSteamid2()),
                            quote(pojo.getRegDatetime(), YYYYMMDD_HHMMSS_PATTERN)
                    ) + "}" + (i + 1 < playerSteamIds.size() ? "," : "")
            );
        }
        System.out.println();
    }

    private String quote(Object value) {
        if (value == null)
            return "null";
        return "\"" + value + "\"";
    }

    private String quote(LocalDateTime value, DateTimeFormatter pattern) {
        if (value == null)
            return "null";
        return "\"" + value.format(pattern) + "\"";
    }

    private List<Field<?>> buildRequestedFields(Table<?> table) {
        if (excludeColumns == null || excludeColumns.isEmpty())
            return Arrays.asList(table.fields());
        Map<Table<?>, List<Field<?>>> excludeColumnsByTable = excludeColumns
                .stream()
                .collect(Collectors.groupingBy(field -> ((TableField<?, ?>) field).getTable()));
        List<Field<?>> excludedFields = excludeColumnsByTable.get(table);
        if (excludedFields == null || excludedFields.isEmpty())
            return Arrays.asList(table.fields());
        return Arrays.stream(table.fields())
                .filter(field -> !excludedFields.contains(field))
                .collect(Collectors.toList());
    }
}