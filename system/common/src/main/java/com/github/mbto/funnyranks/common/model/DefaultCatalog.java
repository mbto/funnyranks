/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model;


import com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.FunnyranksMaxmindCountry;
import com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DefaultCatalog extends CatalogImpl {

    private static final long serialVersionUID = 919675266;

    /**
     * The reference instance of <code></code>
     */
    public static final DefaultCatalog DEFAULT_CATALOG = new DefaultCatalog();

    /**
     * The schema <code>funnyranks</code>.
     */
    public final Funnyranks FUNNYRANKS = com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks.FUNNYRANKS;

    /**
     * The schema <code>funnyranks_stats</code>.
     */
    public final FunnyranksStats FUNNYRANKS_STATS = com.github.mbto.funnyranks.common.model.funnyranks_stats.FunnyranksStats.FUNNYRANKS_STATS;

    /**
     * The schema <code>funnyranks_maxmind_country</code>.
     */
    public final FunnyranksMaxmindCountry FUNNYRANKS_MAXMIND_COUNTRY = com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.FunnyranksMaxmindCountry.FUNNYRANKS_MAXMIND_COUNTRY;

    /**
     * No further instances allowed
     */
    private DefaultCatalog() {
        super("");
    }

    @Override
    public final List<Schema> getSchemas() {
        List result = new ArrayList();
        result.addAll(getSchemas0());
        return result;
    }

    private final List<Schema> getSchemas0() {
        return Arrays.<Schema>asList(
            Funnyranks.FUNNYRANKS,
            FunnyranksStats.FUNNYRANKS_STATS,
            FunnyranksMaxmindCountry.FUNNYRANKS_MAXMIND_COUNTRY);
    }
}
