/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.routines;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats;

import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Playerbysteamid64 extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = 1800302658;

    /**
     * The parameter <code>funnyranks_stats.PlayerBySteamId64.steamId64</code>.
     */
    public static final Parameter<Long> STEAMID64 = Internal.createParameter("steamId64", org.jooq.impl.SQLDataType.BIGINT, false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerBySteamId64.ranks_total</code>.
     */
    public static final Parameter<UInteger> RANKS_TOTAL = Internal.createParameter("ranks_total", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerBySteamId64.lang</code>.
     */
    public static final Parameter<String> LANG = Internal.createParameter("lang", org.jooq.impl.SQLDataType.VARCHAR(5), false, false);

    /**
     * Create a new routine call instance
     */
    public Playerbysteamid64() {
        super("PlayerBySteamId64", FunnyranksStats.FUNNYRANKS_STATS);

        addInParameter(STEAMID64);
        addInParameter(RANKS_TOTAL);
        addInParameter(LANG);
    }

    /**
     * Set the <code>steamId64</code> parameter IN value to the routine
     */
    public void setSteamid64(Long value) {
        setValue(STEAMID64, value);
    }

    /**
     * Set the <code>ranks_total</code> parameter IN value to the routine
     */
    public void setRanksTotal(UInteger value) {
        setValue(RANKS_TOTAL, value);
    }

    /**
     * Set the <code>lang</code> parameter IN value to the routine
     */
    public void setLang(String value) {
        setValue(LANG, value);
    }
}
