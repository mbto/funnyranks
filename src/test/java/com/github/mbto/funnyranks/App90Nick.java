package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.records.PortRecord;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.Player;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerIp;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerName;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.pojos.PlayerSteamid;
import com.github.mbto.funnyranks.handlers.App90;
import com.github.mbto.funnyranks.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mbto.funnyranks.common.BrokerEvent.APPLY_CHANGES;
import static com.github.mbto.funnyranks.common.BrokerEvent.FLUSH_SESSIONS_FROM_FRONTEND;
import static com.github.mbto.funnyranks.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
import static com.github.mbto.funnyranks.common.model.funnyranks_stats.Tables.*;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.timezoneEnumByLiteral;
import static java.util.Arrays.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DependsOn("distributorTE")
@Slf4j
public class App90Nick {
    @Autowired
    private Map<UShort, PortData> portDataByPort;

    @Autowired
    private EventService eventService;
    @Autowired
    private DSLContext funnyRanksAdminDsl;
    @Autowired
    private LogsSender logsSender;
    @Autowired
    private ProjectMaker projectMaker;

    @BeforeClass
    public static void beforeClass() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void beforeTest() {
        truncateTables();
    }

    @After
    public void afterTest() {
//        truncateTables();
    }

    @Test
    public void truncateOnly() {
        truncateTables();
    }

    @Test
    public void server1_27015_27015() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "0", "11", "960", "1", "2021-01-08 13:33:00", "Test server at 27015"}, // 16m 0s
                {"1", "10", "1", "720", "1", "2021-01-08 13:32:00", "Test server at 27015"} // 12m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "12.12.12.12", "2021-01-08 13:32:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 13:32:00"},
                {"2", "2", "Name2", "2021-01-08 13:33:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void another_project_server1_27015_27015() {
        Project project = buildDefaultProject("CS project 1", "funnyranks_stats_project2");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "0", "11", "960", "1", "2021-01-08 13:33:00", "Test server at 27015"}, // 16m 0s
                {"1", "10", "1", "720", "1", "2021-01-08 13:32:00", "Test server at 27015"} // 12m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "12.12.12.12", "2021-01-08 13:32:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 13:32:00"},
                {"2", "2", "Name2", "2021-01-08 13:33:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server1_27015_27015_with_changing_names() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1_changing_names.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "0", "12", "1740", "1", "2021-01-08 13:46:00", "Test server at 27015"}, // 29m 0s
                {"4", "4", "0", "660", "1", "2021-01-08 13:43:00", "Test server at 27015"}, // 11m 0s
                {"1", "5", "0", "600", "1", "2021-01-08 13:45:00", "Test server at 27015"}, // 10m 0s
                {"3", "2", "1", "480", "1", "2021-01-08 13:27:00", "Test server at 27015"} // 8m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "12.12.12.12", "2021-01-08 13:45:00"},
                {"2", "1", "24.24.24.24", "2021-01-08 13:45:00"},
                {"3", "3", "12.12.12.12", "2021-01-08 13:27:00"},
                {"4", "4", "12.12.12.12", "2021-01-08 13:43:00"},
                {"5", "4", "24.24.24.24", "2021-01-08 13:43:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 13:45:00"},
                {"2", "2", "Name2", "2021-01-08 13:46:00"},
                {"3", "3", "Name5", "2021-01-08 13:27:00"},
                {"4", "4", "Name9", "2021-01-08 13:43:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "2", "1:987654", "2021-01-08 13:46:00"}
        });
    }

    @Test
    public void server1_27015_27015_max_ips_steamids() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1_max_ips_steamids.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "0", "0", "2040", "1", "2021-01-08 14:05:00", "Test server at 27015"} // 34m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"3", "1", "127.0.0.3", "2021-01-08 14:05:00"},
                {"4", "1", "127.0.0.4", "2021-01-08 14:05:00"},
                {"5", "1", "127.0.0.5", "2021-01-08 14:05:00"},
                {"6", "1", "127.0.0.6", "2021-01-08 14:05:00"},
                {"7", "1", "127.0.0.7", "2021-01-08 14:05:00"},
                {"8", "1", "127.0.0.8", "2021-01-08 14:05:00"},
                {"9", "1", "127.0.0.9", "2021-01-08 14:05:00"},
                {"10", "1", "127.0.0.10", "2021-01-08 14:05:00"},
                {"11", "1", "127.0.0.11", "2021-01-08 14:05:00"},
                {"12", "1", "127.0.0.12", "2021-01-08 14:05:00"},
                {"13", "1", "127.0.0.13", "2021-01-08 14:05:00"},
                {"14", "1", "127.0.0.14", "2021-01-08 14:05:00"},
                {"15", "1", "127.0.0.15", "2021-01-08 14:05:00"},
                {"16", "1", "127.0.0.16", "2021-01-08 14:05:00"},
                {"17", "1", "127.0.0.17", "2021-01-08 14:05:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 14:05:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"3", "1", "0:3", "2021-01-08 14:05:00"},
                {"4", "1", "0:4", "2021-01-08 14:05:00"},
                {"5", "1", "0:5", "2021-01-08 14:05:00"},
                {"6", "1", "0:6", "2021-01-08 14:05:00"},
                {"7", "1", "0:7", "2021-01-08 14:05:00"},
                {"8", "1", "0:8", "2021-01-08 14:05:00"},
                {"9", "1", "0:9", "2021-01-08 14:05:00"},
                {"10", "1", "0:10", "2021-01-08 14:05:00"},
                {"11", "1", "0:11", "2021-01-08 14:05:00"},
                {"12", "1", "0:12", "2021-01-08 14:05:00"},
                {"13", "1", "0:13", "2021-01-08 14:05:00"},
                {"14", "1", "0:14", "2021-01-08 14:05:00"},
                {"15", "1", "0:15", "2021-01-08 14:05:00"},
                {"16", "1", "0:16", "2021-01-08 14:05:00"},
                {"17", "1", "0:17", "2021-01-08 14:05:00"}
        });
    }

    @Test
    public void server4_27016_27016() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27016, 27016, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27016, 27016);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "17", "11", "449", "1", "2021-01-08 21:25:07", "Test server at 27016"}, // 7m 29s
                {"15", "11", "21", "443", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 23s
                {"13", "10", "16", "443", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 23s
                {"14", "13", "17", "440", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 20s
                {"12", "21", "14", "438", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 18s
                {"11", "8", "22", "438", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 18s
                {"5", "8", "11", "438", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 18s
                {"8", "14", "19", "435", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 15s
                {"4", "12", "10", "435", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 15s
                {"9", "20", "10", "434", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 14s
                {"10", "18", "10", "434", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 14s
                {"1", "14", "17", "422", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 7m 2s
                {"7", "20", "16", "417", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 6m 57s
                {"3", "14", "16", "415", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 6m 55s
                {"16", "13", "17", "400", "1", "2021-01-08 21:24:58", "Test server at 27016"}, // 6m 40s
                {"6", "14", "16", "395", "1", "2021-01-08 21:24:58", "Test server at 27016"} // 6m 35s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "2", "127.0.0.1", "2021-01-08 21:25:07"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "[52 xemaike2h blanil", "2021-01-08 21:24:58"},
                {"2", "2", "Admin", "2021-01-08 21:25:07"},
                {"3", "3", "aromaken1", "2021-01-08 21:24:58"},
                {"4", "4", "BatalOOl", "2021-01-08 21:24:58"},
                {"5", "5", "BoBka’)", "2021-01-08 21:24:58"},
                {"6", "6", "castzOr", "2021-01-08 21:24:58"},
                {"7", "7", "Currv", "2021-01-08 21:24:58"},
                {"8", "8", "haaimbat", "2021-01-08 21:24:58"},
                {"9", "9", "KaRJlSoH", "2021-01-08 21:24:58"},
                {"10", "10", "nameasd", "2021-01-08 21:24:58"},
                {"11", "11", "pravwOw~", "2021-01-08 21:24:58"},
                {"12", "12", "showw", "2021-01-08 21:24:58"},
                {"13", "13", "sonic", "2021-01-08 21:24:58"},
                {"14", "14", "wRa1 wRa1", "2021-01-08 21:24:58"},
                {"15", "15", "yeppi", "2021-01-08 21:24:58"},
                {"16", "16", "~kewAw0w~~", "2021-01-08 21:24:58"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server1_27015_27016_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27016, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27016);
        }, asList(PLAYER.LAST_SERVER_NAME, PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */
        assertPlayers(projectMaker, new String[][]{
                {"2", "0", "22", "1920", "1", "2021-01-08 13:33:00", null}, // 32m 0s
                {"1", "20", "2", "1440", "1", "2021-01-08 13:32:00", null} // 24m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "12.12.12.12", "2021-01-08 13:32:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 13:32:00"},
                {"2", "2", "Name2", "2021-01-08 13:33:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server1_27015_27016_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27016, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27016);
        }, asList(PLAYER.LAST_SERVER_NAME, PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */
        assertPlayers(projectMaker, new String[][]{
                {"1", "20", "2", "1920", "1", "2021-01-08 13:32:00", null}, // 32m 0s
                {"2", "0", "22", "1920", "1", "2021-01-08 13:33:00", null} // 32m 0s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "12.12.12.12", "2021-01-08 13:32:00"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Name1", "2021-01-08 13:32:00"},
                {"2", "2", "Name2", "2021-01-08 13:33:00"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server2_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server2.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "7", "4", "94", "1", "2021-01-08 20:52:15", "Test server at 27015"}, // 1m 34s
                {"2", "0", "8", "89", "1", "2021-01-08 20:52:10", "Test server at 27015"}, // 1m 29s
                {"4", "5", "1", "76", "1", "2021-01-08 20:52:10", "Test server at 27015"}, // 1m 16s
                {"3", "3", "2", "51", "1", "2021-01-08 20:52:10", "Test server at 27015"} // 51s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 20:52:15"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 20:52:15"},
                {"2", "2", "cusoma", "2021-01-08 20:52:10"},
                {"3", "3", "no kill", "2021-01-08 20:52:10"},
                {"4", "4", "timoxatw", "2021-01-08 20:52:10"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server2_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server2.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "7", "5", "221", "1", "2021-01-08 20:58:38", "Test server at 27015"}, // 3m 41s
                {"3", "3", "2", "113", "1", "2021-01-08 20:52:10", "Test server at 27015"}, // 1m 53s
                {"4", "5", "1", "110", "1", "2021-01-08 20:52:10", "Test server at 27015"}, // 1m 50s
                {"2", "0", "8", "104", "1", "2021-01-08 20:52:10", "Test server at 27015"} // 1m 44s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 20:52:15"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 20:52:15"},
                {"2", "2", "cusoma", "2021-01-08 20:52:10"},
                {"3", "3", "no kill", "2021-01-08 20:52:10"},
                {"4", "4", "timoxatw", "2021-01-08 20:52:10"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server3_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server3.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "0", "1", "95", "1", "2021-01-08 20:52:16", "Test server at 27015"}, // 1m 35s
                {"1", "1", "0", "94", "1", "2021-01-08 20:52:15", "Test server at 27015"} // 1m 34s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 20:52:15"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 20:52:15"},
                {"2", "2", "cusoma", "2021-01-08 20:52:16"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server3_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server3.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "1", "0", "127", "1", "2021-01-08 20:52:15", "Test server at 27015"}, // 2m 7s
                {"3", "0", "0", "119", "1", "2021-01-08 20:52:16", "Test server at 27015"}, // 1m 59s
                {"4", "0", "0", "116", "1", "2021-01-08 20:52:16", "Test server at 27015"}, // 1m 56s
                {"2", "0", "1", "110", "1", "2021-01-08 20:52:16", "Test server at 27015"} // 1m 50s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 20:52:15"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 20:52:15"},
                {"2", "2", "cusoma", "2021-01-08 20:52:16"},
                {"3", "3", "no kill", "2021-01-08 20:52:16"},
                {"4", "4", "timoxatw", "2021-01-08 20:52:16"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server4_27015_27015() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"2", "17", "11", "449", "1", "2021-01-08 21:25:07", "Test server at 27015"}, // 7m 29s
                {"15", "11", "21", "443", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 23s
                {"13", "10", "16", "443", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 23s
                {"14", "13", "17", "440", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 20s
                {"12", "21", "14", "438", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 18s
                {"11", "8", "22", "438", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 18s
                {"5", "8", "11", "438", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 18s
                {"8", "14", "19", "435", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 15s
                {"4", "12", "10", "435", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 15s
                {"9", "20", "10", "434", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 14s
                {"10", "18", "10", "434", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 14s
                {"1", "14", "17", "422", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 7m 2s
                {"7", "20", "16", "417", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 6m 57s
                {"3", "14", "16", "415", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 6m 55s
                {"16", "13", "17", "400", "1", "2021-01-08 21:24:58", "Test server at 27015"}, // 6m 40s
                {"6", "14", "16", "395", "1", "2021-01-08 21:24:58", "Test server at 27015"} // 6m 35s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "2", "127.0.0.1", "2021-01-08 21:25:07"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "[52 xemaike2h blanil", "2021-01-08 21:24:58"},
                {"2", "2", "Admin", "2021-01-08 21:25:07"},
                {"3", "3", "aromaken1", "2021-01-08 21:24:58"},
                {"4", "4", "BatalOOl", "2021-01-08 21:24:58"},
                {"5", "5", "BoBka’)", "2021-01-08 21:24:58"},
                {"6", "6", "castzOr", "2021-01-08 21:24:58"},
                {"7", "7", "Currv", "2021-01-08 21:24:58"},
                {"8", "8", "haaimbat", "2021-01-08 21:24:58"},
                {"9", "9", "KaRJlSoH", "2021-01-08 21:24:58"},
                {"10", "10", "nameasd", "2021-01-08 21:24:58"},
                {"11", "11", "pravwOw~", "2021-01-08 21:24:58"},
                {"12", "12", "showw", "2021-01-08 21:24:58"},
                {"13", "13", "sonic", "2021-01-08 21:24:58"},
                {"14", "14", "wRa1 wRa1", "2021-01-08 21:24:58"},
                {"15", "15", "yeppi", "2021-01-08 21:24:58"},
                {"16", "16", "~kewAw0w~~", "2021-01-08 21:24:58"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server4_27015_27025() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27025, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27025);
        }, asList(PLAYER.LAST_SERVER_NAME, PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */
        assertPlayers(projectMaker, new String[][]{
                {"2", "187", "121", "4939", "1", "2021-01-08 21:25:07", null}, // 1h 22m 19s
                {"15", "121", "231", "4873", "1", "2021-01-08 21:24:58", null}, // 1h 21m 13s
                {"13", "110", "176", "4873", "1", "2021-01-08 21:24:58", null}, // 1h 21m 13s
                {"14", "143", "187", "4840", "1", "2021-01-08 21:24:58", null}, // 1h 20m 40s
                {"12", "231", "154", "4818", "1", "2021-01-08 21:24:58", null}, // 1h 20m 18s
                {"11", "88", "242", "4818", "1", "2021-01-08 21:24:58", null}, // 1h 20m 18s
                {"5", "88", "121", "4818", "1", "2021-01-08 21:24:58", null}, // 1h 20m 18s
                {"8", "154", "209", "4785", "1", "2021-01-08 21:24:58", null}, // 1h 19m 45s
                {"4", "132", "110", "4785", "1", "2021-01-08 21:24:58", null}, // 1h 19m 45s
                {"9", "220", "110", "4774", "2", "2021-01-08 21:24:58", null}, // 1h 19m 34s
                {"10", "198", "110", "4774", "2", "2021-01-08 21:24:58", null}, // 1h 19m 34s
                {"1", "154", "187", "4642", "1", "2021-01-08 21:24:58", null}, // 1h 17m 22s
                {"7", "220", "176", "4587", "1", "2021-01-08 21:24:58", null}, // 1h 16m 27s
                {"3", "154", "176", "4565", "1", "2021-01-08 21:24:58", null}, // 1h 16m 5s
                {"16", "143", "187", "4400", "1", "2021-01-08 21:24:58", null}, // 1h 13m 20s
                {"6", "154", "176", "4345", "1", "2021-01-08 21:24:58", null} // 1h 12m 25s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "2", "127.0.0.1", "2021-01-08 21:25:07"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "[52 xemaike2h blanil", "2021-01-08 21:24:58"},
                {"2", "2", "Admin", "2021-01-08 21:25:07"},
                {"3", "3", "aromaken1", "2021-01-08 21:24:58"},
                {"4", "4", "BatalOOl", "2021-01-08 21:24:58"},
                {"5", "5", "BoBka’)", "2021-01-08 21:24:58"},
                {"6", "6", "castzOr", "2021-01-08 21:24:58"},
                {"7", "7", "Currv", "2021-01-08 21:24:58"},
                {"8", "8", "haaimbat", "2021-01-08 21:24:58"},
                {"9", "9", "KaRJlSoH", "2021-01-08 21:24:58"},
                {"10", "10", "nameasd", "2021-01-08 21:24:58"},
                {"11", "11", "pravwOw~", "2021-01-08 21:24:58"},
                {"12", "12", "showw", "2021-01-08 21:24:58"},
                {"13", "13", "sonic", "2021-01-08 21:24:58"},
                {"14", "14", "wRa1 wRa1", "2021-01-08 21:24:58"},
                {"15", "15", "yeppi", "2021-01-08 21:24:58"},
                {"16", "16", "~kewAw0w~~", "2021-01-08 21:24:58"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "2", "0", "20", "1", "2021-01-08 23:42:21", "Test server at 27015"}, // 20s
                {"2", "0", "2", "20", "1", "2021-01-08 23:42:21", "Test server at 27015"} // 20s
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:42:21"},
                {"2", "2", "CeHb^Oaa", "2021-01-08 23:42:21"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "2", "0", "76", "1", "2021-01-08 23:42:21", "Test server at 27015"}, // 1m 16s
                {"2", "0", "2", "51", "1", "2021-01-08 23:42:21", "Test server at 27015"} // 51s
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:42:21"},
                {"2", "2", "CeHb^Oaa", "2021-01-08 23:42:21"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, false, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, false, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "0", "0", "76", "1", "2021-01-08 23:42:21", "Test server at 27015"}, // 1m 16s
                {"2", "0", "0", "51", "1", "2021-01-08 23:42:21", "Test server at 27015"} // 51s
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:42:21"},
                {"2", "2", "CeHb^Oaa", "2021-01-08 23:42:21"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:42:21"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "4", "0", "46", "1", "2021-01-08 23:45:56", "Test server at 27015"}, // 46s
                {"2", "0", "4", "46", "1", "2021-01-08 23:45:56", "Test server at 27015"} // 46s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "255.0.0.142", "2021-01-08 23:45:56"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:45:56"},
                {"2", "2", "desch", "2021-01-08 23:45:56"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "4", "0", "101", "1", "2021-01-08 23:45:56", "Test server at 27015"}, // 1m 41s
                {"2", "0", "4", "88", "1", "2021-01-08 23:45:56", "Test server at 27015"} // 1m 28s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "255.0.0.142", "2021-01-08 23:45:56"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:45:56"},
                {"2", "2", "desch", "2021-01-08 23:45:56"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, false, false, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, false, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "0", "0", "101", "1", "2021-01-08 23:45:56", "Test server at 27015"}, // 1m 41s
                {"2", "0", "0", "88", "1", "2021-01-08 23:45:56", "Test server at 27015"} // 1m 28s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "255.0.0.142", "2021-01-08 23:45:56"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 23:45:56"},
                {"2", "2", "desch", "2021-01-08 23:45:56"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "1", "0:123456", "2021-01-08 23:45:56"}
        });
    }

    @Test
    public void server4_27015_27017_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27025, true, true, true, true);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27017);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
        });
        assertPlayerIps(projectMaker, new String[][]{
        });
        assertPlayerNames(projectMaker, new String[][]{
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server4_27015_27015_dont_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27015, true, true, true, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27015);
        }, asList(PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        assertPlayers(projectMaker, new String[][]{
                {"1", "0", "0", "598", "1", "2021-01-08 21:25:07", "Test server at 27015"} // 9m 58s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 21:25:07"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 21:25:07"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server4_27015_27017_dont_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27025, true, true, true, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27017);
        }, asList(PLAYER.LAST_SERVER_NAME, PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */
        assertPlayers(projectMaker, new String[][]{
                {"1", "0", "0", "1794", "1", "2021-01-08 21:25:07", null} // 29m 54s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "1", "127.0.0.1", "2021-01-08 21:25:07"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "Admin", "2021-01-08 21:25:07"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
        });
    }

    @Test
    public void server4_manual_flush_27014_27018_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "funnyranks_stats");
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            UInteger brokerId = addBroker(transactionalDsl, "broker_1");
            addProject(transactionalDsl, project);
            addPort(transactionalDsl, brokerId, project.getId(), 27015, 27017, true, true, false, false);
        });
        eventService.addEventToDefaultPartition(project.getId(), APPLY_CHANGES, false);
        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4_only_load.log", 27014, 27018);
            for (UShort portValue : portDataByPort.keySet()) {
                try {
                    eventService.flushSessions(portValue, FLUSH_SESSIONS_FROM_FRONTEND, false);
                    log.info("Flush " + portValue + " registered");
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                }
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (Throwable ignored) {
            }
        }, asList(PLAYER.LAST_SERVER_NAME, PLAYER_IP.IP, PLAYER_IP.COUNTRY_NAME, PLAYER_IP.COUNTRY_EMOJI, PLAYER_STEAMID.STEAMID64, PLAYER_STEAMID.STEAMID3));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */
        assertPlayers(projectMaker, new String[][]{
                {"2", "51", "33", "1767", "1", "2021-01-08 21:24:58", null}, // 29m 27s
                {"11", "24", "66", "1350", "1", "2021-01-08 21:24:58", null}, // 22m 30s
                {"3", "42", "48", "1347", "1", "2021-01-08 21:24:58", null}, // 22m 27s
                {"15", "33", "63", "1347", "1", "2021-01-08 21:24:58", null}, // 22m 27s
                {"6", "42", "48", "1344", "1", "2021-01-08 21:24:58", null}, // 22m 24s
                {"13", "30", "48", "1344", "1", "2021-01-08 21:24:58", null}, // 22m 24s
                {"14", "39", "51", "1341", "1", "2021-01-08 21:24:58", null}, // 22m 21s
                {"4", "36", "30", "1341", "1", "2021-01-08 21:24:58", null}, // 22m 21s
                {"12", "63", "42", "1338", "1", "2021-01-08 21:24:58", null}, // 22m 18s
                {"8", "42", "57", "1338", "1", "2021-01-08 21:24:58", null}, // 22m 18s
                {"7", "60", "48", "1335", "1", "2021-01-08 21:24:58", null}, // 22m 15s
                {"16", "39", "51", "1335", "1", "2021-01-08 21:24:58", null}, // 22m 15s
                {"9", "60", "30", "1332", "1", "2021-01-08 21:24:58", null}, // 22m 12s
                {"1", "42", "51", "1332", "1", "2021-01-08 21:24:58", null}, // 22m 12s
                {"10", "54", "30", "1329", "1", "2021-01-08 21:24:58", null}, // 22m 9s
                {"5", "24", "33", "1329", "1", "2021-01-08 21:24:58", null} // 22m 9s
        });
        assertPlayerIps(projectMaker, new String[][]{
                {"1", "2", "127.0.1.1", "2021-01-08 21:24:58"}
        });
        assertPlayerNames(projectMaker, new String[][]{
                {"1", "1", "[52 xemaike2h blanil", "2021-01-08 21:24:58"},
                {"2", "2", "Admin", "2021-01-08 21:24:58"},
                {"3", "3", "aromaken1", "2021-01-08 21:24:58"},
                {"4", "4", "BatalOOl", "2021-01-08 21:24:58"},
                {"5", "5", "BoBka’)", "2021-01-08 21:24:58"},
                {"6", "6", "castzOr", "2021-01-08 21:24:58"},
                {"7", "7", "Currv", "2021-01-08 21:24:58"},
                {"8", "8", "haaimbat", "2021-01-08 21:24:58"},
                {"9", "9", "KaRJlSoH", "2021-01-08 21:24:58"},
                {"10", "10", "nameasd", "2021-01-08 21:24:58"},
                {"11", "11", "pravwOw~", "2021-01-08 21:24:58"},
                {"12", "12", "showw", "2021-01-08 21:24:58"},
                {"13", "13", "sonic", "2021-01-08 21:24:58"},
                {"14", "14", "wRa1 wRa1", "2021-01-08 21:24:58"},
                {"15", "15", "yeppi", "2021-01-08 21:24:58"},
                {"16", "16", "~kewAw0w~~", "2021-01-08 21:24:58"}
        });
        assertPlayerSteamIds(projectMaker, new String[][]{
                {"1", "2", "0:555000", "2021-01-08 21:24:58"}
        });
    }

    private void assertPlayers(ProjectMaker projectMaker,
                               String[][] expectedRaw) {
        List<Player> actualData = projectMaker.getPlayers();
        List<Player> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayerFromRaw)
                .collect(Collectors.toList());
        assertEquals(actualData, expectedData);
    }
    private void assertPlayerIps(ProjectMaker projectMaker, String[][] expectedRaw) {
        List<PlayerIp> actualData = projectMaker.getPlayersIps();
        List<PlayerIp> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersIpsFromRaw)
                .collect(Collectors.toList());
        assertEquals(actualData, expectedData);
    }
    private void assertPlayerNames(ProjectMaker projectMaker, String[][] expectedRaw) {
        List<PlayerName> actualData = projectMaker.getPlayerNames();
        List<PlayerName> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayerNameFromRaw)
                .collect(Collectors.toList());
        assertEquals(actualData, expectedData);
    }
    private void assertPlayerSteamIds(ProjectMaker projectMaker, String[][] expectedRaw) {
        List<PlayerSteamid> actualData = projectMaker.getPlayerSteamIds();
        List<PlayerSteamid> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersSteamIdsFromRaw)
                .collect(Collectors.toList());
        assertEquals(actualData, expectedData);
    }
    private UInteger addBroker(DSLContext transactionalDsl, String brokerName) {
        log.info("Add broker name: " + brokerName);
        return transactionalDsl.insertInto(BROKER)
                .set(BROKER.NAME, brokerName)
                .returning(BROKER.ID).fetchOne().getId();
    }
    public Project buildDefaultProject(String projectName, String projectSchema) {
        Project project = new Project();
        project.setName(projectName);
//        project.setDescription();
        project.setLanguage(ProjectLanguage.en);
        project.setMergeType(ProjectMergeType.Nick);
        project.setDatabaseHostport("127.0.0.1:3306");
        project.setDatabaseSchema(projectSchema);
        project.setDatabaseUsername("funnyranks_stats"); /* grants same as `stats`, but with TRUNCATE */
        project.setDatabasePassword("funnyranks_stats");
        project.setDatabaseServerTimezone(timezoneEnumByLiteral.get("Europe/Moscow"));
        return project;
    }
    private void addProject(DSLContext transactionalDsl, Project project) {
        log.info("Add project name: " + project.getName() + ", hostport: " + project.getDatabaseHostport() + ", schema: " + project.getDatabaseSchema());
        transactionalDsl.insertInto(PROJECT)
                .set(PROJECT.NAME, project.getName())
//                .set(PROJECT.DESCRIPTION, project.getDescription())
                .set(PROJECT.LANGUAGE, project.getLanguage())
                .set(PROJECT.MERGE_TYPE, project.getMergeType())
                .set(PROJECT.DATABASE_HOSTPORT, project.getDatabaseHostport())
                .set(PROJECT.DATABASE_SCHEMA, project.getDatabaseSchema())
                .set(PROJECT.DATABASE_USERNAME, project.getDatabaseUsername())
                .set(PROJECT.DATABASE_PASSWORD, project.getDatabasePassword())
                .set(PROJECT.DATABASE_SERVER_TIMEZONE, project.getDatabaseServerTimezone())
                .returning().fetchOne().into(project);
    }
    private void addPort(DSLContext transactionalDsl,
                         UInteger brokerId,
                         UInteger projectId,
                         int portStart,
                         int portEnd,
                         boolean active,
                         boolean ffa,
                         boolean ignore_bots,
                         boolean start_session_on_action) {
        List<InsertSetMoreStep<PortRecord>> insertSteps = new ArrayList<>(portEnd - portStart + 1);
        for (int port = portStart; port <= portEnd; port++) {
            log.info("Add port " + port);
            InsertSetMoreStep<PortRecord> insertStep = DSL.insertInto(PORT)
                    .set(PORT.BROKER_ID, brokerId)
                    .set(PORT.PROJECT_ID, projectId)
                    .set(PORT.GAME_APP_ID, App90.appId)
                    .set(PORT.VALUE, UShort.valueOf(port))
                    .set(PORT.NAME, "Test server at " + port)
                    .set(PORT.ACTIVE, active)
                    .set(PORT.FFA, ffa)
                    .set(PORT.IGNORE_BOTS, ignore_bots)
                    .set(PORT.START_SESSION_ON_ACTION, start_session_on_action);
            insertSteps.add(insertStep);
        }
        transactionalDsl.batch(insertSteps).execute();
    }

    public void truncateTables() {
        funnyRanksAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            try {
                transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 0");
                transactionalDsl.truncate(BROKER).execute();
                transactionalDsl.truncate(DRIVER_PROPERTY).execute();
                transactionalDsl.truncate(PORT).execute();
                transactionalDsl.truncate(PROJECT).execute();
            } finally {
                try {
                    transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 1");
                } catch (Throwable ignored) {
                }
            }
        });
    }
    /**
     * {"2", "0", "11", "66", "1", "2021-08-01 13:16:08", "Test server 127.0.0.1:27015"}, // 1m 6s
     * {"1", "10", "1", "10", "1", "2021-08-01 13:16:07", "Test server 127.0.0.1:27015"} // 10s
     */
    private Player makePlayerFromRaw(String[] sourceRaw) {
        Player player = new Player();
        player.setId(UInteger.valueOf(sourceRaw[0]));
        player.setKills(UInteger.valueOf(sourceRaw[1]));
        player.setDeaths(UInteger.valueOf(sourceRaw[2]));
        player.setTimeSecs(UInteger.valueOf(sourceRaw[3]));

        if (StringUtils.isNoneBlank(sourceRaw[4]))
            player.setRankId(UInteger.valueOf(sourceRaw[4]));

        if (StringUtils.isNoneBlank(sourceRaw[5]))
            player.setLastseenDatetime(LocalDateTime.parse(sourceRaw[5], YYYYMMDD_HHMMSS_PATTERN));

        if (StringUtils.isNoneBlank(sourceRaw[6]))
            player.setLastServerName(sourceRaw[6]);
        return player;
    }
    /**
     * {"1", "1", "12.12.12.12", "2021-08-01 13:16:07"},
     * {"2", "1", "23.23.23.23", "2021-08-01 13:20:10"}
     */
    private PlayerIp makePlayersIpsFromRaw(String[] sourceRaw) {
        PlayerIp pojo = new PlayerIp();
        pojo.setId(UInteger.valueOf(sourceRaw[0]));
        pojo.setPlayerId(UInteger.valueOf(sourceRaw[1]));
        pojo.setIp4(sourceRaw[2]);
        pojo.setRegDatetime(LocalDateTime.parse(sourceRaw[3], YYYYMMDD_HHMMSS_PATTERN));
        return pojo;
    }
    /**
     * {"1", "1", "Name1", "2021-08-01 13:16:07"},
     * {"2", "1", "Name2", "2021-08-01 13:20:10"}
     */
    private PlayerName makePlayerNameFromRaw(String[] sourceRaw) {
        PlayerName pojo = new PlayerName();
        pojo.setId(UInteger.valueOf(sourceRaw[0]));
        pojo.setPlayerId(UInteger.valueOf(sourceRaw[1]));
        pojo.setName(sourceRaw[2]);
        pojo.setRegDatetime(LocalDateTime.parse(sourceRaw[3], YYYYMMDD_HHMMSS_PATTERN));
        return pojo;
    }
    /**
     * {"1", "1", "0:123123123", "2021-08-01 13:16:07"},
     * {"2", "1", "0:456456456", "2021-08-01 13:20:10"}
     */
    private PlayerSteamid makePlayersSteamIdsFromRaw(String[] sourceRaw) {
        PlayerSteamid pojo = new PlayerSteamid();
        pojo.setId(UInteger.valueOf(sourceRaw[0]));
        pojo.setPlayerId(UInteger.valueOf(sourceRaw[1]));
        pojo.setSteamid2(sourceRaw[2]);
        pojo.setRegDatetime(LocalDateTime.parse(sourceRaw[3], YYYYMMDD_HHMMSS_PATTERN));
        return pojo;
    }
}