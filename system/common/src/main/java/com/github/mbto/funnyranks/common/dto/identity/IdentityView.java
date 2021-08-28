package com.github.mbto.funnyranks.common.dto.identity;

import com.github.jgonian.ipmath.Ipv4;
import com.github.mbto.funnyranks.common.dto.session.ArchivedSessionView;
import com.github.mbto.funnyranks.common.dto.session.Session;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;

import java.time.LocalDateTime;

import static com.github.mbto.funnyranks.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.convertSteamId64ToSteamId2;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.humanLifetime;

@Getter
@Setter
public class IdentityView {
    private String identity;
    private ArchivedSessionView archivedSessionView;
    private String lifeTime;
    private String dates;
    private String ip;
    private String steamId;

    public IdentityView(String identity, ArchivedSessionView archivedSessionView) {
        this.identity = identity;
        this.archivedSessionView = archivedSessionView;
        Session currentOrArchivedSession = archivedSessionView.getArchivedSession();
        LocalDateTime started = currentOrArchivedSession.getStarted();
        LocalDateTime finished = currentOrArchivedSession.getFinished();
        if (finished != null)
            this.lifeTime = humanLifetime(started, finished);
        this.dates = YYYYMMDD_HHMMSS_PATTERN.format(started) + (finished != null ? " - " + YYYYMMDD_HHMMSS_PATTERN.format(finished) : "");
        UInteger ip = currentOrArchivedSession.getIp();
        if (ip != null)
            this.ip = Ipv4.of(ip.longValue()).toString();
        this.steamId = convertSteamId64ToSteamId2(currentOrArchivedSession.getSteamId64());
    }
}