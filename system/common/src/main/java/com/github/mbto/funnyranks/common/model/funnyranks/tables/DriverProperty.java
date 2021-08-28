/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables;


import com.github.mbto.funnyranks.common.model.funnyranks.Funnyranks;
import com.github.mbto.funnyranks.common.model.funnyranks.Indexes;
import com.github.mbto.funnyranks.common.model.funnyranks.Keys;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.records.DriverPropertyRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;


/**
 * Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DriverProperty extends TableImpl<DriverPropertyRecord> {

    private static final long serialVersionUID = -1550929754;

    /**
     * The reference instance of <code>funnyranks.driver_property</code>
     */
    public static final DriverProperty DRIVER_PROPERTY = new DriverProperty();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DriverPropertyRecord> getRecordType() {
        return DriverPropertyRecord.class;
    }

    /**
     * The column <code>funnyranks.driver_property.id</code>.
     */
    public final TableField<DriverPropertyRecord, UInteger> ID = createField("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>funnyranks.driver_property.project_id</code>.
     */
    public final TableField<DriverPropertyRecord, UInteger> PROJECT_ID = createField("project_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>funnyranks.driver_property.key</code>.
     */
    public final TableField<DriverPropertyRecord, String> KEY = createField("key", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>funnyranks.driver_property.value</code>.
     */
    public final TableField<DriverPropertyRecord, String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * Create a <code>funnyranks.driver_property</code> table reference
     */
    public DriverProperty() {
        this(DSL.name("driver_property"), null);
    }

    /**
     * Create an aliased <code>funnyranks.driver_property</code> table reference
     */
    public DriverProperty(String alias) {
        this(DSL.name(alias), DRIVER_PROPERTY);
    }

    /**
     * Create an aliased <code>funnyranks.driver_property</code> table reference
     */
    public DriverProperty(Name alias) {
        this(alias, DRIVER_PROPERTY);
    }

    private DriverProperty(Name alias, Table<DriverPropertyRecord> aliased) {
        this(alias, aliased, null);
    }

    private DriverProperty(Name alias, Table<DriverPropertyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html"));
    }

    public <O extends Record> DriverProperty(Table<O> child, ForeignKey<O, DriverPropertyRecord> key) {
        super(child, key, DRIVER_PROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Funnyranks.FUNNYRANKS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.DRIVER_PROPERTY_DRIVER_PROPERTY_PROJECT_ID_IDX, Indexes.DRIVER_PROPERTY_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<DriverPropertyRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_DRIVER_PROPERTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<DriverPropertyRecord> getPrimaryKey() {
        return Keys.KEY_DRIVER_PROPERTY_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<DriverPropertyRecord>> getKeys() {
        return Arrays.<UniqueKey<DriverPropertyRecord>>asList(Keys.KEY_DRIVER_PROPERTY_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<DriverPropertyRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<DriverPropertyRecord, ?>>asList(Keys.DRIVER_PROPERTY_PROJECT_ID_FK);
    }

    public Project project() {
        return new Project(this, Keys.DRIVER_PROPERTY_PROJECT_ID_FK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverProperty as(String alias) {
        return new DriverProperty(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverProperty as(Name alias) {
        return new DriverProperty(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public DriverProperty rename(String name) {
        return new DriverProperty(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DriverProperty rename(Name name) {
        return new DriverProperty(name, null);
    }
}
