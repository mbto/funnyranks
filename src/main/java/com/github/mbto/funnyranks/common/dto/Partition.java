package com.github.mbto.funnyranks.common.dto;

import lombok.Getter;
import org.jooq.types.UShort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

@Getter
public class Partition {
    private final int partitionId;
    private final LinkedBlockingDeque<Message<?>> partition;
    private final Map<Boolean, Set<UShort>> portsByStatus;

    public Partition(int partitionId) {
        this.partitionId = partitionId;

        /* Integer.MAX_VALUE - Maximum number of Message objects,
           other objects will be rejected by ThreadPoolExecutor.DiscardPolicy() */
        this.partition = new LinkedBlockingDeque<>(Integer.MAX_VALUE);

        this.portsByStatus = new HashMap<>(2, 1f);
        this.portsByStatus.put(true, new HashSet<>());
        this.portsByStatus.put(false, new HashSet<>());
    }

    public void addPort(UShort portValue, boolean isActive) {
        portsByStatus.get(isActive).add(portValue);
    }

    public void removePort(UShort portValue, boolean isActive) {
        portsByStatus.get(isActive).remove(portValue);
    }

    public int countPorts(boolean isActive) {
        return portsByStatus.get(isActive).size();
    }

    public boolean canRemovePartition() {
        return portsByStatus.get(true).size() == 0
                && portsByStatus.get(false).size() == 0;
    }

    public void clearPorts() {
        portsByStatus.get(true).clear();
        portsByStatus.get(false).clear();
    }

    @Override
    public String toString() {
        return "Partition[" + partitionId + "]";
    }
}