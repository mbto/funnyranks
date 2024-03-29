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
public class Top extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = -188406223;

    /**
     * The parameter <code>funnyranks_stats.Top.rows_count</code>.
     */
    public static final Parameter<UInteger> ROWS_COUNT = Internal.createParameter("rows_count", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>funnyranks_stats.Top.lang</code>.
     */
    public static final Parameter<String> LANG = Internal.createParameter("lang", org.jooq.impl.SQLDataType.VARCHAR(5), false, false);

    /**
     * Create a new routine call instance
     */
    public Top() {
        super("Top", FunnyranksStats.FUNNYRANKS_STATS);

        addInParameter(ROWS_COUNT);
        addInParameter(LANG);
    }

    /**
     * Set the <code>rows_count</code> parameter IN value to the routine
     */
    public void setRowsCount(UInteger value) {
        setValue(ROWS_COUNT, value);
    }

    /**
     * Set the <code>lang</code> parameter IN value to the routine
     */
    public void setLang(String value) {
        setValue(LANG, value);
    }
}
