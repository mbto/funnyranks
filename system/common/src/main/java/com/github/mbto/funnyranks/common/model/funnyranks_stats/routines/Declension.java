/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_stats.routines;


import com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Declension extends AbstractRoutine<String> {

    private static final long serialVersionUID = 589234976;

    /**
     * The parameter <code>funnyranks_stats.declension.RETURN_VALUE</code>.
     */
    public static final Parameter<String> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.VARCHAR(32), false, false);

    /**
     * The parameter <code>funnyranks_stats.declension.value</code>.
     */
    public static final Parameter<Integer> VALUE = Internal.createParameter("value", org.jooq.impl.SQLDataType.INTEGER, false, false);

    /**
     * The parameter <code>funnyranks_stats.declension.opt1</code>.
     */
    public static final Parameter<String> OPT1 = Internal.createParameter("opt1", org.jooq.impl.SQLDataType.VARCHAR(32), false, false);

    /**
     * The parameter <code>funnyranks_stats.declension.opt2</code>.
     */
    public static final Parameter<String> OPT2 = Internal.createParameter("opt2", org.jooq.impl.SQLDataType.VARCHAR(32), false, false);

    /**
     * The parameter <code>funnyranks_stats.declension.opt3</code>.
     */
    public static final Parameter<String> OPT3 = Internal.createParameter("opt3", org.jooq.impl.SQLDataType.VARCHAR(32), false, false);

    /**
     * Create a new routine call instance
     */
    public Declension() {
        super("declension", FunnyranksStats.FUNNYRANKS_STATS, org.jooq.impl.SQLDataType.VARCHAR(32));

        setReturnParameter(RETURN_VALUE);
        addInParameter(VALUE);
        addInParameter(OPT1);
        addInParameter(OPT2);
        addInParameter(OPT3);
    }

    /**
     * Set the <code>value</code> parameter IN value to the routine
     */
    public void setValue(Integer value) {
        setValue(VALUE, value);
    }

    /**
     * Set the <code>value</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setValue(Field<Integer> field) {
        setField(VALUE, field);
    }

    /**
     * Set the <code>opt1</code> parameter IN value to the routine
     */
    public void setOpt1(String value) {
        setValue(OPT1, value);
    }

    /**
     * Set the <code>opt1</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setOpt1(Field<String> field) {
        setField(OPT1, field);
    }

    /**
     * Set the <code>opt2</code> parameter IN value to the routine
     */
    public void setOpt2(String value) {
        setValue(OPT2, value);
    }

    /**
     * Set the <code>opt2</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setOpt2(Field<String> field) {
        setField(OPT2, field);
    }

    /**
     * Set the <code>opt3</code> parameter IN value to the routine
     */
    public void setOpt3(String value) {
        setValue(OPT3, value);
    }

    /**
     * Set the <code>opt3</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setOpt3(Field<String> field) {
        setField(OPT3, field);
    }
}
