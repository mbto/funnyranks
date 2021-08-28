package com.github.mbto.funnyranks.common.utils;

import com.github.jgonian.ipmath.Ipv4;
import com.github.mbto.funnyranks.common.dto.Message;
import com.github.mbto.funnyranks.common.dto.Partition;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.identity.Identity;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectDatabaseServerTimezone;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Broker;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.*;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.impl.*;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mbto.funnyranks.common.Constants.*;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class ProjectUtils {
    private static final UIntegerComparator uIntegerComparator = new UIntegerComparator();

    public static final Comparator<PortData> forDashboardPortDataComparator =
            ((Comparator<PortData>) (sd1, sd2) -> uIntegerComparator.compare(
                    sd1.getPort().getProjectId(), sd2.getPort().getProjectId())
            ).reversed()
                    .thenComparing(sd -> sd.getPort().getId());

    public static String extractIp(String ipRaw) {
        if (StringUtils.isBlank(ipRaw))
            return null;
        if (ipRaw.toLowerCase().startsWith("loopback")) {
            return "127.0.0.1";
        } else {
            Matcher matcher = IPADDRESS_PATTERN.matcher(ipRaw);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }

    public static String extractSteamId(String steamIdRaw) {
        if (StringUtils.isBlank(steamIdRaw))
            return null;
        Matcher matcher = STEAMID2_PATTERN.matcher(steamIdRaw);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static final long valveSubtrahendNumber = 76561197960265728L;

    /**
     * https://developer.valvesoftware.com/wiki/SteamID
     *
     * @return // 1 U Individual
     */
    public static Long convertSteamId2ToSteamId64(String steamid2) {
        Matcher matcher = STEAMID2_PATTERN.matcher(steamid2);
        if (matcher.find() && matcher.groupCount() == 2) {
            final long steamid64 = Long.parseLong(matcher.group(1)) + (Long.parseLong(matcher.group(2)) * 2 + valveSubtrahendNumber);
            if (!validateSteamId64Range(steamid64))
                return null;
            return steamid64;
        }
        return null;
    }

    public static String convertSteamId64ToSteamId2(Long steamid64) {
        if (steamid64 == null || !validateSteamId64Range(steamid64)) {
            return null;
        }
        long steamId1 = steamid64 % 2;
        return "STEAM_0:" + steamId1 + ":" + ((steamid64 - valveSubtrahendNumber) - steamId1) / 2;
    }
    /*private static final long valveAddendNumber = 76561197960265727L;
    public static Long convertSteamId3ToSteamId64(String steamid3) {
        Matcher matcher = STEAMID3_PATTERN.matcher(steamid3);
        if(matcher.find() && matcher.groupCount() == 2) {
            long steamid64 = Long.parseLong(matcher.group(1)) + Long.parseLong(matcher.group(2)) + valveAddendNumber;
            if(!validateSteamId64(steamid64))
                return null;

            return steamid64;
        }
        return null;
    }
    public static String convertSteamId64ToSteamId3(long steamid64) {
        if (!validateSteamId64(steamid64)) {
            return null;
        }
        return "[U:1:" + (steamid64 - valveSubtrahendNumber) + "]";
    }*/

    public static long lastSteamId64 = 76561202255233023L;

    private static boolean validateSteamId64Range(long steamid64) {
        return steamid64 >= 76561197960265729L && steamid64 <= lastSteamId64;
    }

    public static boolean validatePort(UShort portValue) {
        if (portValue == null)
            return false;
        int value = portValue.intValue();
        return value >= 1 && value <= 65535;
    }

    /* ProjectDatabaseServerTimezone registry */
    public static final Map<String, ProjectDatabaseServerTimezone> timezoneEnumByLiteral = new LinkedCaseInsensitiveMap<>();

    static {
        for (ProjectDatabaseServerTimezone timezoneEnum : ProjectDatabaseServerTimezone.values()) {
            timezoneEnumByLiteral.put(timezoneEnum.getLiteral(), timezoneEnum);
        }
    }

    public static boolean canMakeIdentity(PortData portData, Session session) {
        ProjectMergeType mergeType = portData.getProject().getMergeType();
        if ((mergeType == ProjectMergeType.IP && session.getIp() == null)
         || (mergeType == ProjectMergeType.Steam_ID && session.getSteamId64() == null)) {
            return false;
        }
        return true;
    }

    public static Map<Identity, List<ArchivedSessionView>> buildIdentitiesContainer(PortData portData,
                                                                                    Map<String, Storage> storageByName,
                                                                                    boolean withCurrentSession,
                                                                                    boolean logIgnored) {
        UShort portValue = portData.getPort().getValue();
        ProjectMergeType mergeType = portData.getProject().getMergeType();
        return storageByName.entrySet()
                .stream()
                .flatMap(entry -> {
                    String name = entry.getKey();
                    Storage storage = entry.getValue();
                    Stream<Session> currentSessionStream = null;
                    if (withCurrentSession) {
                        Session session = storage.getSession(false);
                        if (session != null && session.getStarted() != null) {
                            currentSessionStream = Stream.of(session)
                                    .filter(filteredSession -> {
                                        if (!canMakeIdentity(portData, filteredSession)) {
                                            if (logIgnored) {
                                                String logMsg = buildIgnoredMergeTypeMsg(mergeType, name, filteredSession);
                                                log.info(portValue + " " + logMsg);
                                                portData.addMessage(logMsg);
                                            }
                                            return false;
                                        }
                                        return true;
                                    });
                        }
                    }
                    Stream<Session> sessionStream = storage.getArchivedSessions()
                            .stream()
                            .filter(archivedSession -> {
                                String logMsg;
                                if (!canMakeIdentity(portData, archivedSession)) {
                                    if (logIgnored) {
                                        logMsg = buildIgnoredMergeTypeMsg(mergeType, name, archivedSession);
                                        log.info(portValue + " " + logMsg);
                                        portData.addMessage(logMsg);
                                    }
                                    return false;
                                }
                                if (archivedSession.getStarted() == null || archivedSession.getFinished() == null) {
                                    if (logIgnored) {
                                        logMsg = "Ignored session, due started and finished dateTimes required"
                                                + ": " + archivedSession.toString(name);
                                        log.info(portValue + " " + logMsg);
                                        portData.addMessage(logMsg);
                                    }
                                    return false;
                                }
                                long gamingTimeSecs = archivedSession.calcGamingTimeSecs();
                                if (gamingTimeSecs < 10) {
                                    if (logIgnored) {
                                        logMsg = "Ignored session, due gaming time is " + declension2(gamingTimeSecs, "second")
                                                + ": " + archivedSession.toString(name);
                                        log.info(portValue + " " + logMsg);
                                        portData.addMessage(logMsg);
                                    }
                                    return false;
                                }
                                return true;
                            });
                    if (currentSessionStream != null)
                        sessionStream = Stream.concat(currentSessionStream, sessionStream);
                    return sessionStream.map(archivedSession -> new ArchivedSessionView(name, archivedSession));
                }).collect(Collectors.groupingBy(archivedSession -> {
                    if (mergeType == ProjectMergeType.Nick)
                        return new Identity(archivedSession.getName());
                    else if (mergeType == ProjectMergeType.IP)
                        return new Identity(archivedSession.getArchivedSession().getIp());
                    else if (mergeType == ProjectMergeType.Steam_ID)
                        return new Identity(archivedSession.getArchivedSession().getSteamId64());
                    else
                        throw new UnsupportedOperationException("Unsupported mergeType '" + mergeType + "'");
                }, TreeMap::new, Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    private static String buildIgnoredMergeTypeMsg(ProjectMergeType mergeType, String name, Session session) {
        return "Ignored session, due " + mergeType.getLiteral() + " required for mergeType='" + mergeType + "'"
                + ": " + session.toString(name);
    }

    public static void changeThreadExecutorPoolSizes(ThreadPoolTaskExecutor tpte, int newSize) {
        int oldSize = tpte.getCorePoolSize();
        if (newSize > oldSize) {
            tpte.setMaxPoolSize(newSize > 0 ? newSize : 1);
            tpte.setCorePoolSize(newSize);
        } else if (newSize < oldSize) {
            tpte.setCorePoolSize(newSize);
            tpte.setMaxPoolSize(newSize > 0 ? newSize : 1);
        }
        if (oldSize != newSize)
            //noinspection ConstantConditions
            log.info("Changed '" + tpte.getThreadGroup().getName()
                    + "' pool size from " + oldSize + " to " + newSize);
    }

    public static boolean putLastWithTryes(Partition partition, Message<?> message) {
        return putLastWithTryes(partition.getPartition(), message);
    }

    public static boolean putLastWithTryes(BlockingDeque<Message<?>> partition, Message<?> message) {
        int tryes = 0;
        for (; ; ) {
            try {
                ++tryes;
                partition.putLast(message);
                return true;
            } catch (Throwable e) {
                log.warn("Exception while putLast message " + message + " in partition, " + tryes + "/3");

                if (tryes == 3) {
                    log.warn("Failed putLast message " + message + " in partition");
                    return false;
                }
            }
        }
    }

    public static DefaultDSLContext configurateJooqContext(HikariDataSource hikariDataSource) {
        return configurateJooqContext(hikariDataSource, MILLISECONDS.toSeconds(hikariDataSource.getMaxLifetime()), null, null);
    }

    public static DefaultDSLContext configurateJooqContext(HikariDataSource hikariDataSource, long queryTimeoutSec) {
        return configurateJooqContext(hikariDataSource, queryTimeoutSec, null, null);
    }

    public static DefaultDSLContext configurateJooqContext(HikariDataSource hikariDataSource, String schema, String overridedSchema) {
        return configurateJooqContext(hikariDataSource, MILLISECONDS.toSeconds(hikariDataSource.getMaxLifetime()), schema, overridedSchema);
    }

    public static DefaultDSLContext configurateJooqContext(HikariDataSource hikariDataSource,
                                                           long queryTimeoutSec,
                                                           String schema,
                                                           String overridedSchema) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.set(new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(hikariDataSource)));
        config.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
        config.set(new SpringTransactionProvider(new DataSourceTransactionManager(hikariDataSource)));
        config.setSQLDialect(SQLDialect.MYSQL);
        config.settings().setQueryTimeout(Math.toIntExact(queryTimeoutSec));

        boolean overridingSchema = schema != null && overridedSchema != null && !schema.equalsIgnoreCase(overridedSchema);
        if (overridingSchema) {
            config.settings().setRenderMapping(new RenderMapping()
                    .withSchemata(new MappedSchema()
                            .withInput(schema)
                            .withOutput(overridedSchema))
            );
        }

        DefaultDSLContext defaultDSLContext = new DefaultDSLContext(config);
        if (schema != null)
            defaultDSLContext.setSchema(overridingSchema ? overridedSchema : schema).execute();
        return defaultDSLContext;
    }

    public static HikariDataSource buildHikariDataSource(Project project) {
        return buildHikariDataSource(project, null);
    }

    public static HikariDataSource buildHikariDataSource(Project project, Properties properties) {
        return buildHikariDataSource("pool-" + project.getDatabaseSchema() + " [" + project.getId() + "] " + project.getName(),
                "jdbc:mysql://" + project.getDatabaseHostport() + "/",
                project.getDatabaseSchema(),
                project.getDatabaseUsername(),
                project.getDatabasePassword(),
                properties);
    }

    public static HikariDataSource buildHikariDataSource(String poolName) {
        return buildHikariDataSource(poolName, null, null, null, null, null);
    }

    public static HikariDataSource buildHikariDataSource(String poolName, String schema) {
        return buildHikariDataSource(poolName, null, schema, null, null, null);
    }

    public static HikariDataSource buildHikariDataSource(String poolName, String schema, Properties properties) {
        return buildHikariDataSource(poolName, null, schema, null, null, properties);
    }

    public static HikariDataSource buildHikariDataSource(String poolName,
                                                         String jdbcUrl,
                                                         String schema,
                                                         String username,
                                                         String password,
                                                         Properties properties) {
        HikariDataSource hds = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .type(HikariDataSource.class)
                .build();
        hds.setPoolName(poolName.replace(':', '-'));
        if (jdbcUrl != null)
            hds.setJdbcUrl(jdbcUrl);
        if (schema != null)
            hds.setSchema(schema);
        if (username != null)
            hds.setUsername(username);
        if (password != null)
            hds.setPassword(password);
        /* Override settings from com.zaxxer.hikari.HikariConfig */
        hds.setMaximumPoolSize(2);
        hds.setMinimumIdle(1);

        hds.setConnectionTimeout(SECONDS.toMillis(10));
        hds.setValidationTimeout(SECONDS.toMillis(5));
        hds.setIdleTimeout(SECONDS.toMillis(59));
        hds.setMaxLifetime(hds.getIdleTimeout() + SECONDS.toMillis(1));

        if (properties != null && !properties.isEmpty())
            hds.setDataSourceProperties(properties);

        return hds;
    }

    public static String hikariDataSourceToString(HikariDataSource hds) {
        return "Using datasource settings: jdbcUrl=" + hds.getJdbcUrl()
                + ", schema=" + hds.getSchema()
                + ", username=" + hds.getUsername()
                + ", dataSourceProperties=" + hds.getDataSourceProperties();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int pointwiseUpdateQuery(DSLContext dslContext, TableField pkField, Object pkValue,
                                           List<Pair<Field, ?>> updatableFields) {
        List<Field> selectFields = new ArrayList<>(updatableFields.size() + 2);
        Field aliasedPkField;
        selectFields.add(aliasedPkField = pkField.as("pkey"));
        for (Pair<Field, ?> updatableField : updatableFields) {
            selectFields.add(updatableField.getLeft());
        }
        Table updatableTable = pkField.getTable();
        CommonTableExpression<Record> cte = DSL.name("cte")
                .as(DSL.select(selectFields)
                        .from(updatableTable)
                        .where(pkField.eq(pkValue)));
        Collation collation = DSL.collation("utf8mb4_bin");
        Condition condition = null;
        for (Pair<Field, ?> updatableField : updatableFields) {
            Field targetField = updatableField.getLeft();
            Object newValue = updatableField.getRight();
            Condition newCondition;
            if (newValue == null) {
                newCondition = targetField.isNotNull();
            } else {
                Condition qualifiedCondition;
                if (newValue instanceof String) // 'ddd': `cte`.`description` collate utf8mb4_bin <> 'DDD')
                    qualifiedCondition = cte.field(targetField).collate(collation).notEqual(newValue);
//                    qualifiedCondition = cte.field(targetField).collate(collation).notEqual(DSL.value(newValue).collate(collation));
//                    qualifiedCondition = cte.field(targetField).notEqual(newValue);
                else
                    qualifiedCondition = cte.field(targetField).notEqual(newValue);
                newCondition = DSL.or(cte.field(targetField).isNull(), qualifiedCondition);
            }
            condition = condition != null ? DSL.or(condition, newCondition) : newCondition;
        }
        UpdateSetFirstStep updateStep = dslContext.with(cte).update(updatableTable.join(cte).on(pkField.eq(cte.field(aliasedPkField))));
        for (Pair<Field, ?> updatableField : updatableFields) {
            Field targetField = updatableField.getLeft();
            Object newValue = updatableField.getRight();
            updateStep.set(targetField, newValue);
        }
        return ((UpdateSetMoreStep) updateStep)
                .where(pkField.eq(pkValue), condition)
                .execute();
    }

    public static String gameToString(Game game) {
        return game.getName() + "[" + game.getAppId() + "]";
    }

    public static String brokerToString(Broker broker) {
        return broker.getName() + "[" + broker.getId() + "]";
    }

    public static String projectToString(Project project) {
        return project.getName() + "[" + project.getId() + "]";
    }

    public static String portToString(Port port) {
        return port.getName() + "[" + port.getId() + "]";
    }

    public static UShort extractPort(SocketAddress sa) {
        return UShort.valueOf(((InetSocketAddress) sa).getPort());
    }

    public static String humanBoolean(Boolean value) {
        return (value != null && value) ? "Yes" : "No";
    }

    /*public static String humanLifetime(Timestamp dateStart, Timestamp dateEnd) {
        return humanLifetime(dateStart.toLocalDateTime(),
                Optional.ofNullable(dateEnd)
                        .map(Timestamp::toLocalDateTime)
                        .orElse(null));
    }*/
    public static String humanLifetime(LocalDateTime dateStart, LocalDateTime dateEnd) {
        Duration duration = Duration.between(dateStart, dateEnd);

        long millis = duration.toMillis();
        return humanLifetime(millis);
    }

    public static String humanLifetime(long millis) {
        long hrs = TimeUnit.MILLISECONDS.toHours(millis);
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hrs);
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        StringBuilder sb = new StringBuilder();
        if (hrs > 0)
            sb.append(String.format("%dh ", hrs));
        if (mins > 0)
            sb.append(String.format("%dm ", mins));

        sb.append(String.format("%ds", secs));
        return sb.toString();
    }

    /**
     * @return "@ 2 500 914 bytes (2.39 mb)"
     */
    public static String buildHumanSize(long value) {
        DecimalFormat decLongFormat;
        DecimalFormatSymbols separators = new DecimalFormatSymbols();
        separators.setDecimalSeparator(',');
        separators.setGroupingSeparator(' ');

        decLongFormat = new DecimalFormat("###,###", separators);
        decLongFormat.setGroupingUsed(true);
        decLongFormat.setMinimumFractionDigits(0);
        decLongFormat.setMaximumFractionDigits(0);

        return " @ " + decLongFormat.format(value) + " bytes " + String.format("(%.2f mb)", value / 1024f / 1024f);
    }

    public static long MBtoBytes(int mb) {
        return mb * 1024 * 1024L;
    }

//    public static String declension(long value, String r1, String r2, String r3) {
//        value = Math.abs(value) % 100;
//        if (value > 10 && value < 20)
//            return r3;
//        value = value % 10;
//        if (value > 1 && value < 5)
//            return r2;
//        return value == 1 ? r1 : r3;
//    }

    public static String declension2(long value, String word) {
        return declension2(value, word, "s");
    }

    public static String declension2(long value, String word, String moreOne) {
        return declension2(value, word, moreOne, "");
    }

    public static String declension2(long value, String word, String moreOne, String oneOrZero) {
        return value + " " + word + (value > 1 ? moreOne : oneOrZero);
    }

    public static void fillFakes(Map<UShort, PortData> portDataByPort, Map<UShort, Map<String, Storage>> playersViewByPort) {
        log.info("Making fakes sessions");
        for (Map.Entry<UShort, PortData> entry : portDataByPort.entrySet()) {
            UShort portValue = entry.getKey();
            PortData portData = entry.getValue();

            // session closes with lastTouchDateTime at MessageHandler.java:163, can't exceed lastTouchDateTime
            LocalDateTime lastTouchDateTime = portData.getLastTouchDateTime().toLocalDate().atStartOfDay();

            Map<String, Storage> storageByName = playersViewByPort.get(portValue);
            if (storageByName == null) {
                storageByName = storageByNameContainerSupplier.get();
                playersViewByPort.put(portValue, storageByName);
            }
            for (int playerNum = 0, playersCount = current().nextInt(0, 61); playerNum < playersCount; playerNum++) {
                String randomName;
                do {
                    randomName = StringUtils.capitalize(RandomStringUtils
                            .randomAlphanumeric(current().nextInt(1, 32))
                            .replaceAll("[a-m]", " ")
                            .replaceAll(" {2,}", " ")
                            .trim().toLowerCase());
                    if (StringUtils.isBlank(randomName))
                        randomName = RandomStringUtils.randomAlphanumeric(1);
                } while (storageByName.containsKey(randomName));
                Storage storage = new Storage();
                for (int sessionNum = 0, sessionsCount = current().nextInt(1, 6); sessionNum < sessionsCount; sessionNum++) {
                    if (current().nextBoolean()) {
                        long point = UInteger.MAX_VALUE / current().nextInt(2, 7);
                        storage.getSession(true).setIp(UInteger.valueOf(Ipv4.of(current().nextLong(point - 100, point + 1L)).asBigInteger().longValue()));
                    }
                    if (current().nextBoolean()) {
                        storage.getSession(true).setSteamId64(current().nextLong(lastSteamId64 - 100, lastSteamId64 + 1L));
                    }

                    for (int killNum = 0, killsCount = current().nextInt(0, 11); killNum < killsCount; killNum++) {
                        lastTouchDateTime = lastTouchDateTime.minus(current().nextInt(0, 61), generateChronoUnit());
                        storage.getSession(lastTouchDateTime).upKills();
                    }
                    for (int deathNum = 0, deathCount = current().nextInt(0, 11); deathNum < deathCount; deathNum++) {
                        lastTouchDateTime = lastTouchDateTime.minus(current().nextInt(0, 61), generateChronoUnit());
                        storage.getSession(lastTouchDateTime).upDeaths();
                    }
                    Session session = storage.getSession(false);
                    if (session != null && session.getStarted() != null && current().nextInt(1, sessionsCount + 2) == 1) {
                        storage.onDisconnected(session.getStarted().plus(current().nextInt(0, 61), generateChronoUnit()));
                    }
                }
                if (!storage.getArchivedSessions().isEmpty() || (storage.getSession(false) != null && storage.getSession(false).getStarted() != null))
                    storageByName.put(randomName, storage);
            }
        }
    }

    private static ChronoUnit generateChronoUnit() {
        switch (current().nextInt(1, 4)) {
            case 1: return ChronoUnit.SECONDS;
            case 2: return ChronoUnit.MINUTES;
            case 3: return ChronoUnit.HOURS;
        }
        throw new IllegalStateException("\uD83E\uDD23\uD83D\uDE02");
    }
}