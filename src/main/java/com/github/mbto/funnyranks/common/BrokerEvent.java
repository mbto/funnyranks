package com.github.mbto.funnyranks.common;

public enum BrokerEvent {
    CONSUME_DATAGRAM,
    APPLY_CHANGES,
    FLUSH_SESSIONS_FROM_FRONTEND,
    FLUSH_SESSIONS_FROM_SCHEDULER,
    TERMINATE,
    FLUSH_ALL_SESSIONS_AND_TERMINATE,
}