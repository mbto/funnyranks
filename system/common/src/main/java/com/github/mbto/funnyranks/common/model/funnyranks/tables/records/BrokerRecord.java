/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables.records;


import com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BrokerRecord extends UpdatableRecordImpl<BrokerRecord> implements Record4<UInteger, String, String, LocalDateTime> {

    private static final long serialVersionUID = 1436783102;

    /**
     * Setter for <code>funnyranks.broker.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>funnyranks.broker.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>funnyranks.broker.name</code>. broker name from application.properties
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>funnyranks.broker.name</code>. broker name from application.properties
     */
    @NotNull
    @Size(max = 45)
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>funnyranks.broker.description</code>.
     */
    public void setDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>funnyranks.broker.description</code>.
     */
    @Size(max = 65535)
    public String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>funnyranks.broker.reg_datetime</code>.
     */
    public void setRegDatetime(LocalDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>funnyranks.broker.reg_datetime</code>.
     */
    public LocalDateTime getRegDatetime() {
        return (LocalDateTime) get(3);
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
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UInteger, String, String, LocalDateTime> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UInteger, String, String, LocalDateTime> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return Broker.BROKER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Broker.BROKER.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Broker.BROKER.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field4() {
        return Broker.BROKER.REG_DATETIME;
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
    public String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component4() {
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
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value4() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrokerRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrokerRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrokerRecord value3(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrokerRecord value4(LocalDateTime value) {
        setRegDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrokerRecord values(UInteger value1, String value2, String value3, LocalDateTime value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached BrokerRecord
     */
    public BrokerRecord() {
        super(Broker.BROKER);
    }

    /**
     * Create a detached, initialised BrokerRecord
     */
    public BrokerRecord(UInteger id, String name, String description, LocalDateTime regDatetime) {
        super(Broker.BROKER);

        set(0, id);
        set(1, name);
        set(2, description);
        set(3, regDatetime);
    }
}
