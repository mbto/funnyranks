package com.github.mbto.funnyranks.common.dto.session;

import com.github.jgonian.ipmath.Ipv4;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import com.github.mbto.funnyranks.common.utils.geoip.GeoInfo;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.mbto.funnyranks.common.utils.ProjectUtils.convertSteamId64ToSteamId2;

@Getter
@Setter
public class Session {
    private UInteger ip;
    private Long steamId64;
    private GeoInfo geoInfo;
//    private String geoInfo;
    private boolean ipSetterInvoked;
    private boolean steamId64SetterInvoked;
    private boolean geoInfoSetterInvoked;
    private long kills;
    private long deaths;
    private LocalDateTime started;
    /**
     * finished datetime = PortData created datetime OR last log datetime
     */
    private LocalDateTime finished;

    public void upKills() {
        ++kills;
    }

    public void upDeaths() {
        ++deaths;
    }

    public long calcGamingTimeSecs() {
        if (started == null || finished == null) {
            throw new IllegalStateException("Required started=" + started + " or finished=" + finished + " is null");
        }
        return Duration.between(started, finished).getSeconds();
    }

    public void setStarted(LocalDateTime startedDateTime) {
        if (started == null) {
            if (finished != null && startedDateTime.isAfter(finished)) {
                throw new IllegalArgumentException("Failed set started dateTime: required startedDateTime=" + startedDateTime + " is after finished=" + finished);
            }
            started = startedDateTime;
        }
    }

    public void setFinished(LocalDateTime finishedDateTime) {
        if (finished == null) {
            if (started != null && finishedDateTime.isBefore(started)) {
                throw new IllegalArgumentException("Failed set finished dateTime: required finishedDateTime=" + finishedDateTime + " is before started=" + started);
            }
            finished = finishedDateTime;
        }
    }

    public void setIp(UInteger ip) {
        if (ipSetterInvoked || this.ip != null) {
            return;
        }
        this.ip = ip;
        ipSetterInvoked = true;
    }

    public void setIp(String ip) {
        if (ipSetterInvoked || this.ip != null) {
            return;
        }
        ip = ProjectUtils.extractIp(ip);
        if (ip != null) {
            this.ip = UInteger.valueOf(Ipv4.parse(ip).asBigInteger().longValue());
        }
        ipSetterInvoked = true;
    }

    public void setSteamId64(Long steamId64) {
        if (steamId64SetterInvoked || this.steamId64 != null) {
            return;
        }
        this.steamId64 = steamId64;
        steamId64SetterInvoked = true;
    }

    public void setSteamId64(String steamId2) {
        if (steamId64SetterInvoked || this.steamId64 != null) {
            return;
        }
        steamId2 = ProjectUtils.extractSteamId(steamId2);
        if (steamId2 != null) {
            this.steamId64 = ProjectUtils.convertSteamId2ToSteamId64(steamId2);
        }
        steamId64SetterInvoked = true;
    }

//    public void setGeoInfo(String geoInfo) {
    public void setGeoInfo(GeoInfo geoInfo) {
        if (geoInfoSetterInvoked) {
            return;
        }
        this.geoInfo = geoInfo;
        geoInfoSetterInvoked = true;
    }

    @Override
    public String toString() {
        return "Session{" +
               "ip=" + (ip != null ? Ipv4.of(ip.longValue()).toString() : null) +
               ", steamId=" + convertSteamId64ToSteamId2(steamId64) +
               ", kills=" + kills +
               ", deaths=" + deaths +
               ", started=" + started +
               ", finished=" + finished +
               ", geoInfo=" + geoInfo +
               ", ipSetterInvoked=" + ipSetterInvoked +
               ", steamId64SetterInvoked=" + steamId64SetterInvoked +
               ", geoInfoSetterInvoked=" + geoInfoSetterInvoked +
               '}';
    }

    public String toString(String name, ProjectLanguage projectLanguage) {
        Long gamingTimeSecs = null;
        if (started != null && finished != null) {
            gamingTimeSecs = calcGamingTimeSecs();
        }
        return name
               + ", kills=" + kills
               + ", deaths=" + deaths
               + (gamingTimeSecs != null ? ", time=" + gamingTimeSecs + "s ("
                + ProjectUtils.humanLifetime(gamingTimeSecs * 1000) + ")" : "")
               + ", ip=" + (ip != null ? Ipv4.of(ip.longValue()) : null)
               + ", steamId=" + convertSteamId64ToSteamId2(steamId64)
               + ", geoInfo=" + (geoInfo != null ? geoInfo.locationByProjectLanguage(projectLanguage) : null)
//               + ", ipSetterInvoked=" + ipSetterInvoked
//               + ", steamId64SetterInvoked=" + steamId64SetterInvoked
//               + ", geoInfoSetterInvoked=" + geoInfoSetterInvoked
        ;
    }
}