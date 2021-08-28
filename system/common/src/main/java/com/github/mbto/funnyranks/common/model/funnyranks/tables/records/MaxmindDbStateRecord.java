/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables.records;


import com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState;

import java.time.LocalDate;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;
import org.jooq.types.UInteger;


/**
 * Leave or remove a link for enable/disable auto-updating GeoLite2 country 
 * database:
 * https://github.com/mbto/public_keeper/raw/master/funnyranks/country_en_ru.zip
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MaxmindDbStateRecord extends TableRecordImpl<MaxmindDbStateRecord> implements Record2<LocalDate, UInteger> {

    private static final long serialVersionUID = -588394313;

    /**
     * Setter for <code>funnyranks.maxmind_db_state.date</code>.
     */
    public void setDate(LocalDate value) {
        set(0, value);
    }

    /**
     * Getter for <code>funnyranks.maxmind_db_state.date</code>.
     */
    public LocalDate getDate() {
        return (LocalDate) get(0);
    }

    /**
     * Setter for <code>funnyranks.maxmind_db_state.size</code>.
     */
    public void setSize(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>funnyranks.maxmind_db_state.size</code>.
     */
    public UInteger getSize() {
        return (UInteger) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<LocalDate, UInteger> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<LocalDate, UInteger> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDate> field1() {
        return MaxmindDbState.MAXMIND_DB_STATE.DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return MaxmindDbState.MAXMIND_DB_STATE.SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate component1() {
        return getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component2() {
        return getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate value1() {
        return getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value2() {
        return getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaxmindDbStateRecord value1(LocalDate value) {
        setDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaxmindDbStateRecord value2(UInteger value) {
        setSize(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MaxmindDbStateRecord values(LocalDate value1, UInteger value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MaxmindDbStateRecord
     */
    public MaxmindDbStateRecord() {
        super(MaxmindDbState.MAXMIND_DB_STATE);
    }

    /**
     * Create a detached, initialised MaxmindDbStateRecord
     */
    public MaxmindDbStateRecord(LocalDate date, UInteger size) {
        super(MaxmindDbState.MAXMIND_DB_STATE);

        set(0, date);
        set(1, size);
    }
}
