/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos;


import java.io.Serializable;
import java.time.LocalDate;

import org.jooq.types.UInteger;


/**
 * Leave or remove a link for enable/disable auto-updating GeoLite2 country 
 * database:
 * https://github.com/mbto/public_keeper/raw/master/funnyranks/country_en_ru.zip
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MaxmindDbState implements Serializable {

    private static final long serialVersionUID = -2053006251;

    private LocalDate date;
    private UInteger  size;

    public MaxmindDbState() {}

    public MaxmindDbState(MaxmindDbState value) {
        this.date = value.date;
        this.size = value.size;
    }

    public MaxmindDbState(
        LocalDate date,
        UInteger  size
    ) {
        this.date = date;
        this.size = size;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UInteger getSize() {
        return this.size;
    }

    public void setSize(UInteger size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MaxmindDbState other = (MaxmindDbState) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        }
        else if (!date.equals(other.date))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        }
        else if (!size.equals(other.size))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.date == null) ? 0 : this.date.hashCode());
        result = prime * result + ((this.size == null) ? 0 : this.size.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MaxmindDbState (");

        sb.append(date);
        sb.append(", ").append(size);

        sb.append(")");
        return sb.toString();
    }
}
