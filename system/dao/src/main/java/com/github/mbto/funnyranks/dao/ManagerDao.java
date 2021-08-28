package com.github.mbto.funnyranks.dao;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Manager.MANAGER;

@Repository
@Slf4j
public class ManagerDao {
    @Autowired
    private DSLContext funnyRanksDsl;

    public FunnyRanksManager fetchManager(String username) {
        FunnyRanksManager funnyRanksManager = funnyRanksDsl.selectFrom(MANAGER)
                .where(MANAGER.USERNAME.eq(username))
                .fetchOneInto(FunnyRanksManager.class);
        if (funnyRanksManager != null)
            funnyRanksManager.setupAdditionals();
        return funnyRanksManager;
    }
}