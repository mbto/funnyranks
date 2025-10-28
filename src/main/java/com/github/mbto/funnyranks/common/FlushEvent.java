package com.github.mbto.funnyranks.common;

public enum FlushEvent {
    NEW_GAME_MAP("started new game map"),
    SHUTDOWN_GAME_SERVER("shutdown game server"),
    FRONTEND("frontend"),
    SCHEDULER("scheduler"),
    SHUTDOWN_APPLICATION("shutdown application"),
    ;

    private final String eventName;

    FlushEvent(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return eventName;
    }
}