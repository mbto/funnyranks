package com.github.mbto.funnyranks.common.dto;

import com.github.mbto.funnyranks.common.BrokerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.types.UShort;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> {
    private UShort port;
    private String payload;
    private T pojo;
    private BrokerEvent brokerEvent;

    @Override
    public String toString() {
        return "Message{" +
                "port=" + port +
                ", payload=" + (payload != null ? ("'" + payload + "'") : null) +
                ", pojo=" + (pojo != null ? ("[" + pojo.getClass().getSimpleName() + "]" + pojo) : null) +
                ", brokerEvent=" + brokerEvent +
                '}';
    }
}