package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.broker.Distributor;
import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.IpWrapper;
import com.github.mbto.funnyranks.common.dto.session.Session;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import com.github.mbto.funnyranks.service.MaxMindDbService;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * For manual testing in IntelliJ: add -PManualTestEnabled in Gradle tab "Run Configuration" -> "Arguments"
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DependsOn("distributorTE")
@Slf4j
public class ManualTest {
    @Autowired
    private Map<UShort, PortData> portDataByPort;
    @Autowired
    private Map<UShort, Map<String, Storage>> playersViewByPort;

    @Autowired
    private Distributor distributor;
    @Autowired
    private FunnyRanksDao funnyRanksDao;
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private MaxMindDbService maxMindDbService;

    @BeforeAll
    public static void beforeAll() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    @AfterAll
    public static void afterAll() {
    }

    @Test
    public void testApplyChanges() throws Throwable {
        distributor.applyChanges(null);
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
    }

    @Test
    public void testMakeFakes() {
//        Project project = portDataByPort.get(UShort.valueOf(27015)).getProject();
//        project.setMergeType(ProjectMergeType.Nick);
//        project.setMergeType(ProjectMergeType.IP);
//        project.setMergeType(ProjectMergeType.Steam_ID);
        ProjectUtils.fillFakes(portDataByPort, playersViewByPort);
        List<IpWrapper> ipWrappers = new ArrayList<>();
        for (Map<String, Storage> storageByName : playersViewByPort.values()) {
            for (Storage storage : storageByName.values()) {
                Session session = storage.getSession(false);
                if(session != null) {
                    ipWrappers.add(session.getIpWrapper());
                }
            }
        }
        maxMindDbService.fillIpWrappersWithGeoInfos(ipWrappers, false);
        // after this spring container is shutdown and autoflush sessions
    }

    @Test
    public void testGeoLite2Updater() {
        // Waiting Initializer::applyChanges in @PostConstruct
    }

    @Test
    public void jooqDebug() {
        Record2<Integer, Integer> record =
                funnyRanksDao.fetchPortsCountByAliasRecord(UInteger.valueOf(1), UInteger.valueOf(1));

        System.out.println(record);
    }
}