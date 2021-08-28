/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks;


import com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.Manager;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.Project;


/**
 * Convenience access to all tables in funnyranks
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>funnyranks.broker</code>.
     */
    public static final Broker BROKER = com.github.mbto.funnyranks.common.model.funnyranks.tables.Broker.BROKER;

    /**
     * Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
     */
    public static final DriverProperty DRIVER_PROPERTY = com.github.mbto.funnyranks.common.model.funnyranks.tables.DriverProperty.DRIVER_PROPERTY;

    /**
     * The table <code>funnyranks.game</code>.
     */
    public static final Game GAME = com.github.mbto.funnyranks.common.model.funnyranks.tables.Game.GAME;

    /**
     * The table <code>funnyranks.manager</code>.
     */
    public static final Manager MANAGER = com.github.mbto.funnyranks.common.model.funnyranks.tables.Manager.MANAGER;

    /**
     * Leave or remove a link for enable/disable auto-updating GeoLite2 country database:
https://github.com/mbto/public_keeper/raw/master/funnyranks/country_en_ru.zip
     */
    public static final MaxmindDbState MAXMIND_DB_STATE = com.github.mbto.funnyranks.common.model.funnyranks.tables.MaxmindDbState.MAXMIND_DB_STATE;

    /**
     * The table <code>funnyranks.port</code>.
     */
    public static final Port PORT = com.github.mbto.funnyranks.common.model.funnyranks.tables.Port.PORT;

    /**
     * The table <code>funnyranks.project</code>.
     */
    public static final Project PROJECT = com.github.mbto.funnyranks.common.model.funnyranks.tables.Project.PROJECT;
}
