package com.github.mbto.funnyranks.common.dto.session;

import com.github.mbto.funnyranks.common.utils.geoip.GeoInfo;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

import java.util.Objects;

@Getter
@Setter
public class IpWrapper {
    private UInteger ip;
    private GeoInfo geoInfo;

    public boolean isIpExist() {
        return ip != null;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof IpWrapper ipWrapper)) return false;
        return Objects.equals(geoInfo, ipWrapper.geoInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(geoInfo);
    }
}