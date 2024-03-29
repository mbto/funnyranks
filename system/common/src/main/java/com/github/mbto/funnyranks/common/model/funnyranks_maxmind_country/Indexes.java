/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country;


import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Country;
import com.github.mbto.funnyranks.common.model.funnyranks_maxmind_country.tables.Ipv4;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>funnyranks_maxmind_country</code> 
 * schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index COUNTRY_PRIMARY = Indexes0.COUNTRY_PRIMARY;
    public static final Index IPV4_IPV4_LAST_INT_UNIQUE = Indexes0.IPV4_IPV4_LAST_INT_UNIQUE;
    public static final Index IPV4_IPV4_START_INT_UNIQUE = Indexes0.IPV4_IPV4_START_INT_UNIQUE;
    public static final Index IPV4_IPV4_V_GEONAME_ID_FK_IDX = Indexes0.IPV4_IPV4_V_GEONAME_ID_FK_IDX;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index COUNTRY_PRIMARY = Internal.createIndex("PRIMARY", Country.COUNTRY, new OrderField[] { Country.COUNTRY.GEONAME_ID }, true);
        public static Index IPV4_IPV4_LAST_INT_UNIQUE = Internal.createIndex("ipv4_last_int_UNIQUE", Ipv4.IPV4, new OrderField[] { Ipv4.IPV4.LAST_INT }, true);
        public static Index IPV4_IPV4_START_INT_UNIQUE = Internal.createIndex("ipv4_start_int_UNIQUE", Ipv4.IPV4, new OrderField[] { Ipv4.IPV4.START_INT }, true);
        public static Index IPV4_IPV4_V_GEONAME_ID_FK_IDX = Internal.createIndex("ipv4_v_geoname_id_fk_idx", Ipv4.IPV4, new OrderField[] { Ipv4.IPV4.V_GEONAME_ID }, false);
    }
}
