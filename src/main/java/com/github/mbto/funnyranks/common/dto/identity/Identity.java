package com.github.mbto.funnyranks.common.dto.identity;

import com.github.jgonian.ipmath.Ipv4;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

import java.util.Objects;

import static com.github.mbto.funnyranks.common.utils.ProjectUtils.convertSteamId64ToSteamId2;

@Getter
@Setter
public class Identity implements Comparable<Identity> {
    private Object pojo;

    public Identity(Object pojo) {
        this.pojo = pojo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Object pojo = ((Identity) o).pojo;
        if (this.pojo instanceof String && pojo instanceof String)
            /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
            //noinspection StringOperationCanBeSimplified
            return ((String) this.pojo).toLowerCase().equals(((String) pojo).toLowerCase());
        else
            return this.pojo.equals(pojo);
    }

    @Override
    public int hashCode() {
        if (pojo instanceof String)
            /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
            return Objects.hash(((String) pojo).toLowerCase());
        else
            return Objects.hash(pojo);
    }

    @Override
    public String toString() {
        if (pojo instanceof UInteger)
            return Ipv4.of(((UInteger) pojo).longValue()).toString();
        else if (pojo instanceof Long)
            return convertSteamId64ToSteamId2((Long) pojo);
        return pojo.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public int compareTo(Identity o) {
        if (pojo instanceof String && o.getPojo() instanceof String)
            /* Using compare method from org.springframework.util.LinkedCaseInsensitiveMap#convertKey */
            return ((String) pojo).toLowerCase().compareTo(((String) o.getPojo()).toLowerCase());
        else
            return ((Comparable) pojo).compareTo(o.getPojo());
    }
}