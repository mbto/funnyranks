package com.github.mbto.funnyranks.webapp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@ToString
public class Row<T> {
    private final Object primaryKey;
    private final T pojo;
    @Setter
    private PojoStatus status;
    @Setter
    private PojoStatus previousStatus;

    public Row(T pojo, PojoStatus status) {
        this(null, pojo, status);
    }

    public Row(Object primaryKey, T pojo, PojoStatus status) {
        this.primaryKey = primaryKey;
        this.pojo = pojo;
        this.status = status;
    }
}