package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.MaxmindDbState;
import com.github.mbto.funnyranks.dao.FunnyRanksDao;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState.MAXMIND_DB_STATE;
import static com.github.mbto.funnyranks.webapp.WebUtils.getManagerInfo;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;

@ViewScoped
@Named
@Slf4j
public class ViewEditMaxMindDbState {
    @Autowired
    private DSLContext funnyRanksDsl;
    @Autowired
    private FunnyRanksDao funnyRanksDao;

    @Getter
    @Setter
    private String geolite2CountryDataUrl;
    @Getter
    private MaxmindDbState maxmindDbState;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");
        geolite2CountryDataUrl = funnyRanksDao.fetchMaxmindDbStateComment();
        maxmindDbState = funnyRanksDsl.selectFrom(MAXMIND_DB_STATE)
                .where(MAXMIND_DB_STATE.DATE.isNotNull(),
                        MAXMIND_DB_STATE.SIZE.isNotNull())
                .fetchOneInto(MaxmindDbState.class);
    }

    public void save() {
        log.info("Attempting to save geolite2CountryDataUrl " + getManagerInfo());
        FacesContext fc = FacesContext.getCurrentInstance();
        int localChangesCounter = 0;
        try {
            funnyRanksDsl.commentOnTable(MAXMIND_DB_STATE)
                    .is(geolite2CountryDataUrl)
                    .execute();
            localChangesCounter = 1;
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("URL saved in table `" + MAXMIND_DB_STATE.getName() + "` comment",
                    localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save URL in table `" + MAXMIND_DB_STATE.getName() + "` comment",
                    e.toString()));
        }
    }
}