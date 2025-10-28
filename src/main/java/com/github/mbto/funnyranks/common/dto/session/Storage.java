package com.github.mbto.funnyranks.common.dto.session;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Storage {
    private Session session;
    @Getter
    private final List<Session> archivedSessions = new ArrayList<>();

    public Session getSession(LocalDateTime startedDateTime) {
        Session session = getSession(true);
        session.setStarted(startedDateTime);
        return session;
    }

    public Session getSession(boolean create) {
        if (session == null || session.getFinished() != null) {
            if (session != null) {
                archivedSessions.add(session);
            }
            return session = create ? new Session() : null;
        }
        return session;
    }

    public void onDisconnected(LocalDateTime finishedDateTime) {
        if (session != null) {
            session.setFinished(finishedDateTime);
            archivedSessions.add(session);
            session = null;
        }
    }

    public int calcSessionsCount() {
        Session session = getSession(false);
        return archivedSessions.size() + (session != null && session.getStarted() != null ? 1 : 0);
    }

    public void clearStorage() {
        session = null;
        archivedSessions.clear();
    }

    @Override
    public String toString() {
        return "Storage{" +
                "session=" + session +
                ", archivedSessions=" + archivedSessions
                .stream()
                .map(Session::toString)
                .collect(Collectors.joining("\n\t", "\n\t", "\n")) +
                '}';
    }
}