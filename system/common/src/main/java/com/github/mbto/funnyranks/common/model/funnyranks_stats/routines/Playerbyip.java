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
public class Playerbyip extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = -706798782;

    /**
     * The parameter <code>funnyranks_stats.PlayerByIp.ip</code>.
     */
    public static final Parameter<UInteger> IP = Internal.createParameter("ip", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerByIp.ranks_total</code>.
     */
    public static final Parameter<UInteger> RANKS_TOTAL = Internal.createParameter("ranks_total", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.PlayerByIp.lang</code>.
     */
    public static final Parameter<String> LANG = Internal.createParameter("lang", org.jooq.impl.SQLDataType.VARCHAR(5), false, false);

    /**
     * Create a new routine call instance
     */
    public Playerbyip() {
        super("PlayerByIp", FunnyranksStats.FUNNYRANKS_STATS);

        addInParameter(IP);
        addInParameter(RANKS_TOTAL);
        addInParameter(LANG);
    }

    /**
     * Set the <code>ip</code> parameter IN value to the routine
     */
    public void setIp(UInteger value) {
        setValue(IP, value);
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