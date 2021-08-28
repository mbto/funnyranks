package com.github.mbto.funnyranks.common.utils;

import org.jooq.types.UInteger;

import java.util.Comparator;

public class UIntegerComparator implements Comparator<UInteger> {
    @Override
    public int compare(UInteger o1, UInteger o2) {
        return o1.compareTo(o2);
    }
}