/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.History;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HistoryRecord extends UpdatableRecordImpl<HistoryRecord> implements Record5<UInteger, UInteger, UInteger, UInteger, LocalDateTime> {

    private static final long serialVersionUID = 382563001;

    /**
     * Setter for <code>funnyranks_stats.history.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>funnyranks_stats.history.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>funnyranks_stats.history.player_id</code>.
     */
    public void setPlayerId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>funnyranks_stats.history.player_id</code>.
     */
    @NotNull
    public UInteger getPlayerId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>funnyranks_stats.history.old_rank_id</code>.
     */
    public void setOldRankId(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>funnyranks_stats.history.old_rank_id</code>.
     */
    public UInteger getOldRankId() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>funnyranks_stats.history.new_rank_id</code>.
     */
    public void setNewRankId(UInteger value) {
        set(3, value);
    }

    /**
     * Getter for <code>funnyranks_stats.history.new_rank_id</code>.
     */
    public UInteger getNewRankId() {
        return (UInteger) get(3);
    }

    /**
     * Setter for <code>funnyranks_stats.history.reg_datetime</code>.
     */
    public void setRegDatetime(LocalDateTime value) {
        set(4, value);
    }

    /**
     * Getter for <code>funnyranks_stats.history.reg_datetime</code>.
     */
    public LocalDateTime getRegDatetime() {
        return (LocalDateTime) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UInteger, UInteger, UInteger, UInteger, LocalDateTime> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UInteger, UInteger, UInteger, UInteger, LocalDateTime> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return History.HISTORY.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return History.HISTORY.PLAYER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field3() {
        return History.HISTORY.OLD_RANK_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field4() {
        return History.HISTORY.NEW_RANK_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field5() {
        return History.HISTORY.REG_DATETIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component2() {
        return getPlayerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component3() {
        return getOldRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component4() {
        return getNewRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component5() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value2() {
        return getPlayerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value3() {
        return getOldRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value4() {
        return getNewRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value5() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord value2(UInteger value) {
        setPlayerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord value3(UInteger value) {
        setOldRankId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord value4(UInteger value) {
        setNewRankId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord value5(LocalDateTime value) {
        setRegDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HistoryRecord values(UInteger value1, UInteger value2, UInteger value3, UInteger value4, LocalDateTime value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HistoryRecord
     */
    public HistoryRecord() {
        super(History.HISTORY);
    }

    /**
     * Create a detached, initialised HistoryRecord
     */
    public HistoryRecord(UInteger id, UInteger playerId, UInteger oldRankId, UInteger newRankId, LocalDateTime regDatetime) {
        super(History.HISTORY);

        set(0, id);
        set(1, playerId);
        set(2, oldRankId);
        set(3, newRankId);
        set(4, regDatetime);
    }
}
