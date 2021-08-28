/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.records;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.tables.PlayerIp;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerIpRecord extends UpdatableRecordImpl<PlayerIpRecord> implements Record7<UInteger, UInteger, UInteger, String, String, String, LocalDateTime> {

    private static final long serialVersionUID = -1605392530;

    /**
     * Setter for <code>funnyranks_stats.player_ip.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.player_id</code>.
     */
    public void setPlayerId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.player_id</code>.
     */
    @NotNull
    public UInteger getPlayerId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.ip</code>.
     */
    public void setIp(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.ip</code>.
     */
    @NotNull
    public UInteger getIp() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.ip4</code>. Auto-generated IP format v4 - AAA.BBB.CCC.DDD
     */
    public void setIp4(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.ip4</code>. Auto-generated IP format v4 - AAA.BBB.CCC.DDD
     */
    @Size(max = 15)
    public String getIp4() {
        return (String) get(3);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.country_name</code>.
     */
    public void setCountryName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.country_name</code>.
     */
    @Size(max = 50)
    public String getCountryName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.country_emoji</code>.
     */
    public void setCountryEmoji(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.country_emoji</code>.
     */
    @Size(max = 2)
    public String getCountryEmoji() {
        return (String) get(5);
    }

    /**
     * Setter for <code>funnyranks_stats.player_ip.reg_datetime</code>.
     */
    public void setRegDatetime(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>funnyranks_stats.player_ip.reg_datetime</code>.
     */
    public LocalDateTime getRegDatetime() {
        return (LocalDateTime) get(6);
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
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<UInteger, UInteger, UInteger, String, String, String, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<UInteger, UInteger, UInteger, String, String, String, LocalDateTime> valuesRow() {
        return (Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return PlayerIp.PLAYER_IP.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return PlayerIp.PLAYER_IP.PLAYER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field3() {
        return PlayerIp.PLAYER_IP.IP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return PlayerIp.PLAYER_IP.IP4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return PlayerIp.PLAYER_IP.COUNTRY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return PlayerIp.PLAYER_IP.COUNTRY_EMOJI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field7() {
        return PlayerIp.PLAYER_IP.REG_DATETIME;
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
        return getIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getIp4();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getCountryName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getCountryEmoji();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component7() {
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
        return getIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getIp4();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getCountryName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getCountryEmoji();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value7() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value2(UInteger value) {
        setPlayerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value3(UInteger value) {
        setIp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value4(String value) {
        setIp4(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value5(String value) {
        setCountryName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value6(String value) {
        setCountryEmoji(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord value7(LocalDateTime value) {
        setRegDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerIpRecord values(UInteger value1, UInteger value2, UInteger value3, String value4, String value5, String value6, LocalDateTime value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PlayerIpRecord
     */
    public PlayerIpRecord() {
        super(PlayerIp.PLAYER_IP);
    }

    /**
     * Create a detached, initialised PlayerIpRecord
     */
    public PlayerIpRecord(UInteger id, UInteger playerId, UInteger ip, String ip4, String countryName, String countryEmoji, LocalDateTime regDatetime) {
        super(PlayerIp.PLAYER_IP);

        set(0, id);
        set(1, playerId);
        set(2, ip);
        set(3, ip4);
        set(4, countryName);
        set(5, countryEmoji);
        set(6, regDatetime);
    }
}
