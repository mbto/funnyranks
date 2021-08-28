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
public class Playerbyname extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = -21175252;

    /**
     * The parameter <code>funnyranks_stats.PlayerByName.name</code>.
     */
    public static final Parameter<String> NAME = Internal.createParameter("name", org.jooq.impl.SQLDataType.VARCHAR(31), false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerByName.ranks_total</code>.
     */
    public static final Parameter<UInteger> RANKS_TOTAL = Internal.createParameter("ranks_total", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerByName.lang</code>.
     */
    public static final Parameter<String> LANG = Internal.createParameter("lang", org.jooq.impl.SQLDataType.VARCHAR(5), false, false);

    /**
     * Create a new routine call instance
     */
    public Playerbyname() {
        super("PlayerByName", FunnyranksStats.FUNNYRANKS_STATS);

        addInParameter(NAME);
        addInParameter(RANKS_TOTAL);
        addInParameter(LANG);
    }

    /**
     * Set the <code>name</code> parameter IN value to the routine
     */
    public void setName_(String value) {
        setValue(NAME, value);
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