package com.github.mbto.funnyranks.common.dto;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.DriverProperty;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Game;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Port;
import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Project;
import com.github.mbto.funnyranks.common.utils.ProjectUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.github.mbto.funnyranks.common.Constants.SERVER_DATA_MESSAGES_MAX;
import static com.github.mbto.funnyranks.common.utils.ProjectUtils.humanBoolean;

@Getter
@Setter
public class PortData {
    private Game game;
    private Port port;
    private Project project;
    private LocalDateTime nextFlushDateTime;
    private List<DriverProperty> driverProperties;
    /**
     * PortData created datetime & last log datetime
     */
    private LocalDateTime lastTouchDateTime;
    private List<Pair<LocalDateTime, String>> messages = new CopyOnWriteArrayList<>();

    public boolean isPortActive() {
        return port != null && port.getActive();
    }

    public Properties convertDriverPropertiesToProperties() {
        if (driverProperties == null || driverProperties.isEmpty())
            return null;
        Properties properties = new Properties(driverProperties.size());
        for (DriverProperty driverProperty : driverProperties) {
            properties.put(driverProperty.getKey(), driverProperty.getValue());
        }
        return properties;
    }

    public void updateNextFlushDateTime() {
        nextFlushDateTime = LocalDateTime.now().plusMinutes(1);
    }

    public void addMessage(String message) {
        messages.add(Pair.of(LocalDateTime.now(), message));
        reduceMessagesCount();
    }

    public void addMessages(Collection<String> messages) {
        LocalDateTime now = LocalDateTime.now();
        this.messages.addAll(messages.stream()
                .map(str -> Pair.of(now, str))
                .collect(Collectors.toList()));

        reduceMessagesCount();
    }

    private void reduceMessagesCount() {
        int size = messages.size();
        if (size > SERVER_DATA_MESSAGES_MAX) {
            messages.subList(0, size - SERVER_DATA_MESSAGES_MAX).clear();
        }
    }

    /**
     * used in Distributor.java:429
     */
    public String toString(boolean addActive, boolean addGame) {
        return (addActive ? (isPortActive() ? "[ACTIVE]" : "[NOT ACTIVE]") + " " : "")
                + (addGame ? ProjectUtils.gameToString(game) + ", " : "")
                + port.getValue() + " " + ProjectUtils.portToString(port)
                + ", " + ProjectUtils.projectToString(project) + " (Lang: " + project.getLanguage().getLiteral() + ", Merge type: " + project.getMergeType().getLiteral() + ")"
                + ", FFA: " + humanBoolean(port.getFfa())
                + ", Ignore bots: " + humanBoolean(port.getIgnoreBots())
                + ", Start session on action: " + humanBoolean(port.getStartSessionOnAction())
                ;
    }

    @Override
    public String toString() {
        return toString(true, true);
    }
}