/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.routines;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CalculateRankId extends AbstractRoutine<UInteger> {

    private static final long serialVersionUID = 473444930;

    /**
     * The parameter <code>funnyranks_stats.calculate_rank_id.RETURN_VALUE</code>.
     */
    public static final Parameter<UInteger> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.calculate_rank_id.kills</code>.
     */
    public static final Parameter<UInteger> KILLS = Internal.createParameter("kills", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.calculate_rank_id.deaths</code>.
     */
    public static final Parameter<UInteger> DEATHS = Internal.createParameter("deaths", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.calculate_rank_id.time_secs</code>.
     */
    public static final Parameter<UInteger> TIME_SECS = Internal.createParameter("time_secs", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * Create a new routine call instance
     */
    public CalculateRankId() {
        super("calculate_rank_id", FunnyranksStats.FUNNYRANKS_STATS, org.jooq.impl.SQLDataType.INTEGERUNSIGNED);

        setReturnParameter(RETURN_VALUE);
        addInParameter(KILLS);
        addInParameter(DEATHS);
        addInParameter(TIME_SECS);
    }

    /**
     * Set the <code>kills</code> parameter IN value to the routine
     */
    public void setKills(UInteger value) {
        setValue(KILLS, value);
    }

    /**
     * Set the <code>kills</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setKills(Field<UInteger> field) {
        setField(KILLS, field);
    }

    /**
     * Set the <code>deaths</code> parameter IN value to the routine
     */
    public void setDeaths(UInteger value) {
        setValue(DEATHS, value);
    }

    /**
     * Set the <code>deaths</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setDeaths(Field<UInteger> field) {
        setField(DEATHS, field);
    }

    /**
     * Set the <code>time_secs</code> parameter IN value to the routine
     */
    public void setTimeSecs(UInteger value) {
        setValue(TIME_SECS, value);
    }

    /**
     * Set the <code>time_secs</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setTimeSecs(Field<UInteger> field) {
        setField(TIME_SECS, field);
    }
}
