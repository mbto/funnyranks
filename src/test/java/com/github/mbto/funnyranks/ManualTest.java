package com.github.mbto.funnyranks;

import com.github.mbto.funnyranks.common.dto.PortData;
import com.github.mbto.funnyranks.common.dto.session.Storage;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * For manual testing in IntelliJ: add -PManualTestEnabled in Gradle tab "Run Configuration" -> "Arguments"
 */
@SpringBootTest
@RunWith(SpringRunner.class)
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

    @BeforeClass
    public static void beforeClass() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void testApplyChanges() throws Throwable {
        distributor.applyChanges(null, false);
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
    }

    @Test
    public void testMakeFakes() {
//        Project project = portDataByPort.get(UShort.valueOf(27015)).getProject();
//        project.setMergeType(ProjectMergeType.Nick);
//        project.setMergeType(ProjectMergeType.IP);
//        project.setMergeType(ProjectMergeType.Steam_ID);
        ProjectUtils.fillFakes(portDataByPort, playersViewByPort);
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