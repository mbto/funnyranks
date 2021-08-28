/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country;


import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Country;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Ipv4;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.records.CountryRecord;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.records.Ipv4Record;

import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>funnyranks_maxmind_country</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<CountryRecord> KEY_COUNTRY_PRIMARY = UniqueKeys0.KEY_COUNTRY_PRIMARY;
    public static final UniqueKey<Ipv4Record> KEY_IPV4_IPV4_START_INT_UNIQUE = UniqueKeys0.KEY_IPV4_IPV4_START_INT_UNIQUE;
    public static final UniqueKey<Ipv4Record> KEY_IPV4_IPV4_LAST_INT_UNIQUE = UniqueKeys0.KEY_IPV4_IPV4_LAST_INT_UNIQUE;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<Ipv4Record, CountryRecord> IPV4_V_GEONAME_ID_FK = ForeignKeys0.IPV4_V_GEONAME_ID_FK;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 {
        public static final UniqueKey<CountryRecord> KEY_COUNTRY_PRIMARY = Internal.createUniqueKey(Country.COUNTRY, "KEY_country_PRIMARY", Country.COUNTRY.GEONAME_ID);
        public static final UniqueKey<Ipv4Record> KEY_IPV4_IPV4_START_INT_UNIQUE = Internal.createUniqueKey(Ipv4.IPV4, "KEY_ipv4_ipv4_start_int_UNIQUE", Ipv4.IPV4.START_INT);
        public static final UniqueKey<Ipv4Record> KEY_IPV4_IPV4_LAST_INT_UNIQUE = Internal.createUniqueKey(Ipv4.IPV4, "KEY_ipv4_ipv4_last_int_UNIQUE", Ipv4.IPV4.LAST_INT);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<Ipv4Record, CountryRecord> IPV4_V_GEONAME_ID_FK = Internal.createForeignKey(com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.Keys.KEY_COUNTRY_PRIMARY, Ipv4.IPV4, "ipv4_v_geoname_id_fk", Ipv4.IPV4.V_GEONAME_ID);
    }
}