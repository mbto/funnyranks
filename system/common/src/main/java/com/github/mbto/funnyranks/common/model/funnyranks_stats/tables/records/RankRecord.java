/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.Rank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
public class RankRecord extends UpdatableRecordImpl<RankRecord> implements Record5<UInteger, UInteger, String, String, String> {

    private static final long serialVersionUID = 865973085;

    /**
     * Setter for <code>funnyranks_stats.rank.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>funnyranks_stats.rank.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>funnyranks_stats.rank.level</code>.
     */
    public void setLevel(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>funnyranks_stats.rank.level</code>.
     */
    @NotNull
    public UInteger getLevel() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>funnyranks_stats.rank.kaomoji</code>.
     */
    public void setKaomoji(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>funnyranks_stats.rank.kaomoji</code>.
     */
    @NotNull
    @Size(max = 60)
    public String getKaomoji() {
        return (String) get(2);
    }

    /**
     * Setter for <code>funnyranks_stats.rank.name_ru</code>.
     */
    public void setNameRu(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>funnyranks_stats.rank.name_ru</code>.
     */
    @NotNull
    @Size(max = 60)
    public String getNameRu() {
        return (String) get(3);
    }

    /**
     * Setter for <code>funnyranks_stats.rank.name_en</code>.
     */
    public void setNameEn(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>funnyranks_stats.rank.name_en</code>.
     */
    @NotNull
    @Size(max = 60)
    public String getNameEn() {
        return (String) get(4);
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
    public Row5<UInteger, UInteger, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UInteger, UInteger, String, String, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return Rank.RANK.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return Rank.RANK.LEVEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Rank.RANK.KAOMOJI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Rank.RANK.NAME_RU;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return Rank.RANK.NAME_EN;
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
        return getLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getKaomoji();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getNameRu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getNameEn();
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
        return getLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getKaomoji();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getNameRu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getNameEn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord value2(UInteger value) {
        setLevel(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord value3(String value) {
        setKaomoji(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord value4(String value) {
        setNameRu(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord value5(String value) {
        setNameEn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RankRecord values(UInteger value1, UInteger value2, String value3, String value4, String value5) {
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
     * Create a detached RankRecord
     */
    public RankRecord() {
        super(Rank.RANK);
    }

    /**
     * Create a detached, initialised RankRecord
     */
    public RankRecord(UInteger id, UInteger level, String kaomoji, String nameRu, String nameEn) {
        super(Rank.RANK);

        set(0, id);
        set(1, level);
        set(2, kaomoji);
        set(3, nameRu);
        set(4, nameEn);
    }
}
