package com.github.mbto.funnyranks.common.dto.session;

import com.github.jgonian.ipmath.Ipv4;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.github.mbto.funnyranks.common.utils.ProjectUtils.convertSteamId64ToSteamId2;

@Getter
@Setter
public class Session {
    private IpWrapper ipWrapper = new IpWrapper();
    private Long steamId64;
    private boolean isBot;
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

    @SuppressWarnings("UnusedReturnValue")
    public boolean setIp(UInteger ip) {
        if (ipWrapper.isIpExist()) {
            return false;
        }
        ipWrapper.setIp(ip);
        return true;
    }

    public boolean setIp(String ip) {
        if(StringUtils.isBlank(ip)) {
            return false;
        }
        ip = ProjectUtils.extractIp(ip);
        if (ip != null) {
            ipWrapper.setIp(UInteger.valueOf(Ipv4.parse(ip).asBigInteger().longValue()));
        }
        return ipWrapper.isIpExist();
    }

    public void setSteamId64(Long steamId64) {
        if (this.steamId64 != null) {
            return;
        }
        this.steamId64 = steamId64;
    }

    public void setSteamId64(String steamId2) {
        if(StringUtils.isBlank(steamId2)) {
            return;
        }
        steamId2 = ProjectUtils.extractSteamId(steamId2);
        if (steamId2 != null) {
            this.steamId64 = ProjectUtils.convertSteamId2ToSteamId64(steamId2);
        }
    }

    public void setIsBot(String steamId2) {
        if(StringUtils.isBlank(steamId2)) {
            return;
        }
        this.isBot = ProjectUtils.isAuthBOT(steamId2);
    }

    @Override
    public String toString() {
        return "Session{" +
               "ip=" + (ipWrapper.isIpExist() ? Ipv4.of(ipWrapper.getIp().longValue()).toString() : null) +
               ", steamId=" + convertSteamId64ToSteamId2(steamId64) +
               ", isBot=" + isBot +
               ", kills=" + kills +
               ", deaths=" + deaths +
               ", started=" + started +
               ", finished=" + finished +
               ", geoInfo=" + ipWrapper.getGeoInfo() +
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
               + ", ip=" + (ipWrapper.isIpExist() ? Ipv4.of(ipWrapper.getIp().longValue()) : null)
               + ", steamId=" + convertSteamId64ToSteamId2(steamId64)
               + ", isBot=" + isBot
               + ", geoInfo=" + (ipWrapper.getGeoInfo() != null ? ipWrapper.getGeoInfo().locationByProjectLanguage(projectLanguage) : null)
        ;
    }
}