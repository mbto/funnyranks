package com.github.mbto.funnyranks.dao;

import com.github.mbto.funnyranks.common.dto.FunnyRanksData;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Game.GAME;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;
import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;

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
}