package com.github.mbto.funnyranks.webapp.view;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.webapp.PojoStatus;
import com.github.mbto.funnyranks.webapp.Row;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.model.funnyranks.tables.Game.GAME;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.*;
import static com.github.mbto.funnyranks.webapp.PojoStatus.*;
import static com.github.mbto.funnyranks.webapp.WebUtils.*;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@ViewScoped
@Named
@Slf4j
public class ViewGame {
    @Autowired
    private DSLContext funnyRanksDsl;

    @Getter
    private List<Row<Game>> currentRows;

    @Getter
    private boolean addServerBtnDisabled;

    private int localChangesCounter;

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        fetchGames();
    }

    private void fetchGames() {
        currentRows = funnyRanksDsl.selectFrom(GAME)
                .fetchInto(Game.class)
                .stream()
                .map(game -> new Row<>(game.getAppId(), game, EXISTED))
                .collect(Collectors.toList());
    }

    public void validateAppId(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (!isNumeric(value))
            throw makeValidatorException(value, "");
        UInteger appId = UInteger.valueOf(value);
        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");
        if (log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentRows.size=" + currentRows.size());
        for (int i = 0; i < currentRows.size(); i++) {
            if (i == rowIndexVar)
                continue;
            Row<Game> row = currentRows.get(i);
            Game game = row.getPojo();
            if (appId.equals(game.getAppId()))
                throw makeValidatorException(value, "App ID already exists at game " + gameToString(game));
        }
    }

    public void validateListenerPort(FacesContext context, UIComponent component, String value) throws ValidatorException {
        UShort listenerPort;
        try {
            listenerPort = UShort.valueOf(value);
            if (!validatePort(listenerPort))
                throw new RuntimeException();
        } catch (Throwable e) {
            throw makeValidatorException(value, "");
        }
        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");
        if (log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentRows.size=" + currentRows.size());
        for (int i = 0; i < currentRows.size(); i++) {
            if (i == rowIndexVar)
                continue;
            Row<Game> row = currentRows.get(i);
            Game game = row.getPojo();
            if (listenerPort.equals(game.getListenerPort()))
                throw makeValidatorException(value, "Listener port already exists at game " + gameToString(game));
        }
    }

    public void save() {
        FunnyRanksManager funnyRanksManager;
        if((funnyRanksManager = canManageBroker(true)) == null) {
            addServerBtnDisabled = false;
            return;
        }
        log.info("Attempting to save games " + getManagerInfo(funnyRanksManager));
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;
        try {
            List<Object> toRemoveGameIds = new ArrayList<>();
            for (Iterator<Row<Game>> iterator = currentRows.iterator(); iterator.hasNext(); ) {
                Row<Game> row = iterator.next();
                Game game = row.getPojo();
                if (row.getStatus() == TO_REMOVE) {
                    if (row.getPrimaryKey() != null) {
                        toRemoveGameIds.add(row.getPrimaryKey());
                        log.info("Deleting game " + gameToString(game) + " " + getManagerInfo(funnyRanksManager));
                    }
                    iterator.remove();
                } else if (row.getStatus() == NEW) {
                    if (isBlank(game.getName())) {
                        iterator.remove();
                    }
                }
            }
            if (!toRemoveGameIds.isEmpty() || !currentRows.isEmpty()) {
                funnyRanksDsl.transaction(config -> {
                    DSLContext transactionalDsl = DSL.using(config);
                    if (!toRemoveGameIds.isEmpty()) {
                        localChangesCounter += transactionalDsl.deleteFrom(GAME)
                                .where(GAME.APP_ID.in(toRemoveGameIds))
                                .execute();
                    }
                    for (Row<Game> row : currentRows) {
                        Game game = row.getPojo();
                        PojoStatus pojoStatus = row.getStatus();
                        if (pojoStatus == CHANGED) {
                            log.info("Updating game " + gameToString(game) + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += pointwiseUpdateQuery(transactionalDsl, GAME.APP_ID, row.getPrimaryKey(),
                                    Arrays.asList(
                                            Pair.of(GAME.APP_ID, game.getAppId()),
                                            Pair.of(GAME.NAME, game.getName()),
                                            Pair.of(GAME.LISTENER_PORT, game.getListenerPort()))
                            );
                        } else if (pojoStatus == NEW) {
                            log.info("Inserting game " + gameToString(game) + " " + getManagerInfo(funnyRanksManager));
                            localChangesCounter += transactionalDsl.insertInto(GAME)
                                    .set(GAME.APP_ID, game.getAppId())
                                    .set(GAME.NAME, game.getName())
                                    .set(GAME.LISTENER_PORT, game.getListenerPort())
                                    .execute();
                        }
                    }
                });
            }
//            changesCounter.increment(localChangesCounter);
            fc.addMessage("msgs", new FacesMessage("Games saved", localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save games",
                    e.toString()));
        } finally {
            addServerBtnDisabled = false;
            fetchGames();
        }
    }

    public void onRowEdit(RowEditEvent event) {
        //noinspection unchecked
        Row<Game> row = (Row<Game>) event.getObject();
        if (row.getPrimaryKey() != null) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if (currentRows.get(currentRows.size() - 1).equals(row)) {
            addServerBtnDisabled = false;
        }
        if (log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddGame() {
        if (log.isDebugEnabled())
            log.debug("\nonAddGame");

        Game game = new Game();
        currentRows.add(new Row<>(game, NEW));
        addServerBtnDisabled = true;
    }

    public void onRestoreRow(Row<Game> row) {
        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if (log.isDebugEnabled())
            log.debug("\nonRestoreRow " + row);
    }

    public void onRemoveRow(Row<Game> row) {
        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if (log.isDebugEnabled())
            log.debug("\nonRemoveRow " + row);
    }
}