/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.types.UInteger;
import org.jooq.types.UShort;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Port implements Serializable {

    private static final long serialVersionUID = 1228357379;

    private UInteger id;
    private UInteger brokerId;
    private UInteger projectId;
    private UInteger gameAppId;
    private UShort   value;
    private String   name;
    private Boolean  active;
    private Boolean  ffa;
    private Boolean  ignoreBots;
    private Boolean  startSessionOnAction;

    public Port() {}

    public Port(Port value) {
        this.id = value.id;
        this.brokerId = value.brokerId;
        this.projectId = value.projectId;
        this.gameAppId = value.gameAppId;
        this.value = value.value;
        this.name = value.name;
        this.active = value.active;
        this.ffa = value.ffa;
        this.ignoreBots = value.ignoreBots;
        this.startSessionOnAction = value.startSessionOnAction;
    }

    public Port(
        UInteger id,
        UInteger brokerId,
        UInteger projectId,
        UInteger gameAppId,
        UShort   value,
        String   name,
        Boolean  active,
        Boolean  ffa,
        Boolean  ignoreBots,
        Boolean  startSessionOnAction
    ) {
        this.id = id;
        this.brokerId = brokerId;
        this.projectId = projectId;
        this.gameAppId = gameAppId;
        this.value = value;
        this.name = name;
        this.active = active;
        this.ffa = ffa;
        this.ignoreBots = ignoreBots;
        this.startSessionOnAction = startSessionOnAction;
    }

    public UInteger getId() {
        return this.id;
    }

    public void setId(UInteger id) {
        this.id = id;
    }

    @NotNull
    public UInteger getBrokerId() {
        return this.brokerId;
    }

    public void setBrokerId(UInteger brokerId) {
        this.brokerId = brokerId;
    }

    @NotNull
    public UInteger getProjectId() {
        return this.projectId;
    }

    public void setProjectId(UInteger projectId) {
        this.projectId = projectId;
    }

    @NotNull
    public UInteger getGameAppId() {
        return this.gameAppId;
    }

    public void setGameAppId(UInteger gameAppId) {
        this.gameAppId = gameAppId;
    }

    @NotNull
    public UShort getValue() {
        return this.value;
    }

    public void setValue(UShort value) {
        this.value = value;
    }

    @NotNull
    @Size(max = 31)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getFfa() {
        return this.ffa;
    }

    public void setFfa(Boolean ffa) {
        this.ffa = ffa;
    }

    public Boolean getIgnoreBots() {
        return this.ignoreBots;
    }

    public void setIgnoreBots(Boolean ignoreBots) {
        this.ignoreBots = ignoreBots;
    }

    public Boolean getStartSessionOnAction() {
        return this.startSessionOnAction;
    }

    public void setStartSessionOnAction(Boolean startSessionOnAction) {
        this.startSessionOnAction = startSessionOnAction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Port other = (Port) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (brokerId == null) {
            if (other.brokerId != null)
                return false;
        }
        else if (!brokerId.equals(other.brokerId))
            return false;
        if (projectId == null) {
            if (other.projectId != null)
                return false;
        }
        else if (!projectId.equals(other.projectId))
            return false;
        if (gameAppId == null) {
            if (other.gameAppId != null)
                return false;
        }
        else if (!gameAppId.equals(other.gameAppId))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        }
        else if (!value.equals(other.value))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (active == null) {
            if (other.active != null)
                return false;
        }
        else if (!active.equals(other.active))
            return false;
        if (ffa == null) {
            if (other.ffa != null)
                return false;
        }
        else if (!ffa.equals(other.ffa))
            return false;
        if (ignoreBots == null) {
            if (other.ignoreBots != null)
                return false;
        }
        else if (!ignoreBots.equals(other.ignoreBots))
            return false;
        if (startSessionOnAction == null) {
            if (other.startSessionOnAction != null)
                return false;
        }
        else if (!startSessionOnAction.equals(other.startSessionOnAction))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.brokerId == null) ? 0 : this.brokerId.hashCode());
        result = prime * result + ((this.projectId == null) ? 0 : this.projectId.hashCode());
        result = prime * result + ((this.gameAppId == null) ? 0 : this.gameAppId.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.active == null) ? 0 : this.active.hashCode());
        result = prime * result + ((this.ffa == null) ? 0 : this.ffa.hashCode());
        result = prime * result + ((this.ignoreBots == null) ? 0 : this.ignoreBots.hashCode());
        result = prime * result + ((this.startSessionOnAction == null) ? 0 : this.startSessionOnAction.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Port (");

        sb.append(id);
        sb.append(", ").append(brokerId);
        sb.append(", ").append(projectId);
        sb.append(", ").append(gameAppId);
        sb.append(", ").append(value);
        sb.append(", ").append(name);
        sb.append(", ").append(active);
        sb.append(", ").append(ffa);
        sb.append(", ").append(ignoreBots);
        sb.append(", ").append(startSessionOnAction);

        sb.append(")");
        return sb.toString();
    }
}
