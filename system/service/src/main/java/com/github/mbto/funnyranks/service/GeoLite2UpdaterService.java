package com.github.mbto.funnyranks.service;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.MaxmindDbState;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState.MAXMIND_DB_STATE;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.FunnyranksMaxmindCountry.FUNNYRANKS_MAXMIND_COUNTRY;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.Tables.IPV4;
import static com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Country.COUNTRY;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE;

@Service
@Lazy(false)
@Slf4j
public class GeoLite2UpdaterService {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private FunnyRanksDao funnyRanksDao;
    @Autowired
    private HikariDataSource funnyRanksDataSource;
    @Autowired
    private BrokerHolder brokerHolder;

    private static final Path outputDirPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("funnyranks");

    public void update() {
        if(brokerHolder.isTestEnvironment())
            return;
        String comment = funnyRanksDao.fetchMaxmindDbStateComment();
        Consumer<String> printSkipUpdatingWarnFunc = (String msg) -> {
            log.warn("Skip updating MaxMind GeoLite2 country database, due " + msg);
        };
        if (comment == null) {
            printSkipUpdatingWarnFunc.accept("table `" + MAXMIND_DB_STATE.getName() + "` doesn't have comment");
            return;
        }
        Matcher matcher = Pattern.compile("http[^\\r\\n]*").matcher(comment);
        if (!matcher.find()) {
            printSkipUpdatingWarnFunc.accept("http/https source URL not founded in comment '" + comment + "' from table `" + MAXMIND_DB_STATE.getName() + "`");
            return;
        }
        String geolite2CountryDataUrl = matcher.group();
        if (StringUtils.isBlank(geolite2CountryDataUrl)) {
            printSkipUpdatingWarnFunc.accept("invalid extracted URL '" + geolite2CountryDataUrl + "' from comment '" + comment + "' from table `" + MAXMIND_DB_STATE.getName() + "`");
            return;
        }
        log.info("Started updating MaxMind GeoLite2 country database");
        try {
            if (!Files.isDirectory(outputDirPath))
                Files.createDirectories(outputDirPath);
            funnyRanksDsl.transaction(config -> {
                DSLContext transactionalDslContext = DSL.using(config);
                try {
                    transactionalDslContext.execute("LOCK TABLES `" + FUNNYRANKS.getName() + "`.`" + MAXMIND_DB_STATE.getName() + "` WRITE");
                    MaxmindDbState archiveState = transactionalDslContext
                            .select(MAXMIND_DB_STATE.DATE,
                                    MAXMIND_DB_STATE.SIZE
                            ).from(MAXMIND_DB_STATE)
                            .orderBy(MAXMIND_DB_STATE.DATE.desc())
                            .limit(1)
                            .forUpdate()
                            .fetchOneInto(MaxmindDbState.class);
                    boolean recordExisted = archiveState != null;
                    // http request in transaction for simple synchronization ${outputDirPath}/country_en_ru.zip archive
                    archiveState = update(archiveState, geolite2CountryDataUrl);
                    if (archiveState != null) {
                        if (recordExisted) {
                            transactionalDslContext.update(MAXMIND_DB_STATE)
                                    .set(MAXMIND_DB_STATE.DATE, archiveState.getDate())
                                    .set(MAXMIND_DB_STATE.SIZE, archiveState.getSize())
                                    .execute();
                        } else {
                            transactionalDslContext.insertInto(MAXMIND_DB_STATE)
                                    .set(MAXMIND_DB_STATE.DATE, archiveState.getDate())
                                    .set(MAXMIND_DB_STATE.SIZE, archiveState.getSize())
                                    .execute();
                        }
                        log.info("Finished updating MaxMind GeoLite2 country database dated '" + archiveState.getDate().format(ISO_DATE) + "'");
                    }
                } finally {
                    try {
                        transactionalDslContext.execute("UNLOCK TABLES");
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable e) {
            log.warn("Failed updating MaxMind GeoLite2 country database", e);
        }
    }

    private MaxmindDbState update(MaxmindDbState archiveState, String geolite2CountryDataUrl) throws Throwable {
        log.info("Requesting " + geolite2CountryDataUrl);
        HttpRequest headRequest = HttpRequest.newBuilder(URI.create(geolite2CountryDataUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> headResponse = httpClient.send(headRequest, BodyHandlers.ofString());
        String tipInvalidSource = "Use a zip archive from another http(s) source";
        if (headResponse.statusCode() != 200)
            throw new IllegalStateException("Response code not 200, but " + headResponse.statusCode() + ". " + tipInvalidSource);
        HttpHeaders headers = headResponse.headers();
        String contentType = headers.firstValue("content-type") // "application/zip"
                .orElseThrow(() -> new IllegalStateException("Empty content-type header. " + tipInvalidSource));
        if (!contentType.equalsIgnoreCase("application/zip"))
            throw new UnsupportedOperationException("Unsupported content-type '" + contentType + "'. " + tipInvalidSource);
        long currentArchiveSize = headers.firstValueAsLong("content-length") // "2500914"
                .orElseThrow(() -> new IllegalStateException("Empty content-length header. " + tipInvalidSource));

        if (archiveState != null && archiveState.getSize() != null && archiveState.getSize().longValue() == currentArchiveSize) {
            log.info("Skip updating MaxMind GeoLite2 country database, due archive size is up-to-date" + buildHumanSize(currentArchiveSize));
            return null;
        }
        if (log.isDebugEnabled())
            log.debug("Extracted from headers: content-length" + buildHumanSize(currentArchiveSize));
        Path existedArchivePath = outputDirPath.resolve("country_en_ru.zip");
        // C:\\Users\\user\\AppData\\Local\\Temp\\funnyranks\\country_en_ru.zip
        if (!Files.isRegularFile(existedArchivePath) || currentArchiveSize != Files.size(existedArchivePath)) {
            log.info("Downloading archive to '" + existedArchivePath + "'");
            HttpRequest getRequest = HttpRequest.newBuilder(URI.create(geolite2CountryDataUrl)).GET().build();
            HttpResponse<Path> responseFile = httpClient.send(getRequest, BodyHandlers.ofFile(existedArchivePath, TRUNCATE_EXISTING, CREATE, WRITE));
            if (responseFile.statusCode() != 200)
                throw new IllegalStateException("Response code not 200, but " + headResponse.statusCode() + ". " + tipInvalidSource);
            existedArchivePath = responseFile.body();
        } else {
            log.info("Archive with same size already exists in '" + existedArchivePath + "'");
        }
        /* sort required: country_create.sql ipv4_create.sql
           country_000.sql ipv4_000.sql ipv4_001.sql ipv4_002.sql
           country_indexes.sql ipv4_indexes.sql */
        Comparator<Path> comparator = Comparator.comparing(path -> path.getFileName().toString());
        Set<Path> creates = new TreeSet<>(comparator);
        Set<Path> datas = new TreeSet<>(comparator);
        Set<Path> indexes = new TreeSet<>(comparator);
        LocalDate currentArchiveDate;
        long bytesCountPerFile;
        Path uniqueExtractOutputDirPath;
        try (ZipFile zf = new ZipFile(existedArchivePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            String comment = zf.getComment();
            if (StringUtils.isBlank(comment))
                throw new IllegalStateException("Archive comment is required");

            Pattern extractPattern = Pattern.compile(".*Sources from '.*(\\d{8}).*'.*"); // Sources from 'GeoLite2-Country-CSV_20210713.zip'
            Matcher matcher = extractPattern.matcher(comment);
            if (!matcher.find())
                throw new RuntimeException("Failed extract 'Sources from' regex group");
            currentArchiveDate = LocalDate.parse(matcher.group(1), BASIC_ISO_DATE); // 20210713

            extractPattern = Pattern.compile(".*Max megabytes count per file: (\\d+).*"); // Max megabytes count per file: 64
            matcher = extractPattern.matcher(comment);
            if (!matcher.find())
                throw new RuntimeException("Failed extract 'Max megabytes count per file' regex group");
            bytesCountPerFile = MBtoBytes(Integer.parseInt(matcher.group(1)));

            if (archiveState != null && archiveState.getDate() != null && archiveState.getDate().isAfter(currentArchiveDate)) {
                log.info("Skip updating MaxMind GeoLite2 country database, due previous uploaded archive date is up-to-date '"
                        + archiveState.getDate().format(ISO_DATE) + "'");
                return null;
            }
            if (archiveState == null)
                archiveState = new MaxmindDbState();
            archiveState.setDate(currentArchiveDate);
            archiveState.setSize(UInteger.valueOf(currentArchiveSize));
            log.info("Applying MaxMind GeoLite2 country database with date '" + currentArchiveDate.format(ISO_DATE) + "'");
            uniqueExtractOutputDirPath = outputDirPath.resolve(UUID.randomUUID().toString());
            if (!Files.isDirectory(uniqueExtractOutputDirPath))
                Files.createDirectories(uniqueExtractOutputDirPath);
            extractPattern = Pattern.compile(".*\\d{3}.*"); // data pattern
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getSize() == 0)
                    continue;
                String name = zipEntry.getName();
                if (!(name.startsWith("country_") || name.startsWith("ipv4_")))
                    continue;

                int type;
                if (name.contains("create"))
                    type = 1;
                else if (extractPattern.matcher(name).matches())
                    type = 2;
                else if (name.contains("indexes"))
                    type = 3;
                else
                    continue;

                Path scriptFilePath = uniqueExtractOutputDirPath.resolve(name);
                try (BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(zipEntry))) {
                    Files.copy(bis, scriptFilePath, REPLACE_EXISTING);
                }
                switch (type) {
                    case 1:
                        creates.add(scriptFilePath);
                        break;
                    case 2:
                        datas.add(scriptFilePath);
                        break;
                    case 3:
                        indexes.add(scriptFilePath);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported type " + type + " of file '" + name + "'");
                }
            }
        }
        log.info("Replacing `" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "` schema");
        try {
            Long max_allowed_packet = funnyRanksDsl.resultQuery("select @@max_allowed_packet").fetchOneInto(Long.class);
            if (max_allowed_packet == null || max_allowed_packet < bytesCountPerFile) {
                log.info("Updating 'max_allowed_packet' variable to " + bytesCountPerFile);
                funnyRanksDsl.execute("SET GLOBAL max_allowed_packet=" + bytesCountPerFile);
            }
            try (HikariDataSource hds = buildHikariDataSource("funnyranks-geolite2-updater-pool",
                    funnyRanksDataSource.getJdbcUrl(),
                    FUNNYRANKS_MAXMIND_COUNTRY.getName(),
                    funnyRanksDataSource.getUsername(),
                    funnyRanksDataSource.getPassword(),
                    funnyRanksDataSource.getDataSourceProperties())) {
                log.info(hikariDataSourceToString(hds));

                DSLContext maxmindDslContext = configurateJooqContext(hds);
                try {
                    maxmindDslContext.execute("SET NAMES utf8mb4");
                    maxmindDslContext.execute("SET FOREIGN_KEY_CHECKS=0");
                    maxmindDslContext.dropSchemaIfExists(FUNNYRANKS_MAXMIND_COUNTRY).execute();
                    maxmindDslContext.execute("CREATE SCHEMA `" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    maxmindDslContext.setSchema(FUNNYRANKS_MAXMIND_COUNTRY).execute();

                    for (Path create : creates) {
                        maxmindDslContext.execute(Files.readString(create, UTF_8));
                    }
                    maxmindDslContext.transaction(config -> {
                        DSLContext transactionalDsl = DSL.using(config);
                        try {
                            transactionalDsl.execute("LOCK TABLES " +
                                    String.join(", ",
                                            "`" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "`.`" + COUNTRY.getName() + "` WRITE",
                                            "`" + FUNNYRANKS_MAXMIND_COUNTRY.getName() + "`.`" + IPV4.getName() + "` WRITE"
                                    )
                            );
                            for (Path data : datas) {
                                transactionalDsl.execute(Files.readString(data, UTF_8));
                            }
                        } catch (Throwable e) {
                            try {
                                transactionalDsl.execute("UNLOCK TABLES");
                            } catch (Throwable ignored) {
                            }
                            throw e;
                        }
                    });
                    try {
                        for (Path index : indexes) {
                            maxmindDslContext.execute(Files.readString(index, UTF_8));
                        }
                    } finally {
                        try {
                            maxmindDslContext.execute("UNLOCK TABLES");
                        } catch (Throwable ignored) {
                        }
                    }
                } finally {
                    try {
                        maxmindDslContext.execute("SET FOREIGN_KEY_CHECKS=1");
                    } catch (Throwable ignored) {
                    }
                }
            }
            return archiveState;
        } finally {
            Stream.of(creates, datas, indexes)
                    .flatMap(Collection::stream)
                    .forEach(e -> {
                        try {
                            Files.delete(e);
                        } catch (Throwable ignored) {
                        }
                    });
            try {
                Files.delete(uniqueExtractOutputDirPath);
            } catch (Throwable ignored) {
            }
        }
    }
}
/*
SELECT table_comment
    FROM INFORMATION_SCHEMA.TABLES
    WHERE table_schema='funnyranks' AND table_name='maxmind_db_state';

SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'funnyranks_maxmind_country'
    AND table_name = 'country';

cache-control: max-age=300
content-security-policy: default-src 'none'; style-src 'unsafe-inline'; sandbox
content-type: application/zip
etag: W/"d9fe43c0ac6ecc0609590efa66596b7f58b160759657d6bd925c35901edeb071"
strict-transport-security: max-age=31536000
x-content-type-options: nosniff
x-frame-options: deny
x-xss-protection: 1; mode=block
x-github-request-id: 7F88:B283:192707A:1A73A89:61058E1C
accept-ranges: bytes
via: 1.1 varnish
vary: Authorization,Accept-Encoding
access-control-allow-origin: *
source-age: 0
content-length: 2500914
X-Firefox-Spdy: h2
date: Sat, 31 Jul 2021 18:13:29 GMT
x-served-by: cache-hel6821-HEL
x-timer: S1627755209.304913,VS0,VE173
x-fastly-request-id: 73ffb7183c4d75956702180573e3510763dee655
expires: Sat, 31 Jul 2021 18:18:29 GMT
x-cache: HIT
x-cache-hits: 1

2021-07-15 14:20:33
Auto-generated by MaxMind GeoIP2 csv2sql Converter v1.0
https://github.com/mbto/maxmind-geoip2-csv2sql-converter
Summary:
  Edition ID: GeoLite2-Country-CSV
  DBMS name: mysql
  Profile name: funnyranks
Arguments of converting:
  IP version: v4
  Locales: en,ru
  Locations filter: no filter
  Max megabytes count per file: 64
  Max records per line: 100
  Values count per insert: no limit
Sources from 'GeoLite2-Country-CSV_20210713.zip'
  'GeoLite2-Country-Locations-en.csv'
  'GeoLite2-Country-Locations-ru.csv'
  'GeoLite2-Country-Blocks-IPv4.csv'
Stats:
  country: 252
  country includes which unknown: 2
  ipv4: 340547
*/