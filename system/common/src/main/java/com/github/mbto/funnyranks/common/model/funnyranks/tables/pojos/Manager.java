/*
 * This file is generated by jOOQ.
 */
package com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos;


import com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Manager implements Serializable {

    private static final long serialVersionUID = -1387866771;

    private UInteger      id;
    private String        username;
    private String        password;
    private String        description;
    private ManagerRole   role;
    private Boolean       active;
    private String        projectIds;
    private LocalDateTime regDatetime;

    public Manager() {}

    public Manager(Manager value) {
        this.id = value.id;
        this.username = value.username;
        this.password = value.password;
        this.description = value.description;
        this.role = value.role;
        this.active = value.active;
        this.projectIds = value.projectIds;
        this.regDatetime = value.regDatetime;
    }

    public Manager(
        UInteger      id,
        String        username,
        String        password,
        String        description,
        ManagerRole   role,
        Boolean       active,
        String        projectIds,
        LocalDateTime regDatetime
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.description = description;
        this.role = role;
        this.active = active;
        this.projectIds = projectIds;
        this.regDatetime = regDatetime;
    }

    public UInteger getId() {
        return this.id;
    }

    public void setId(UInteger id) {
        this.id = id;
    }

    @NotNull
    @Size(max = 31)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    @Size(max = 60)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Size(max = 65535)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ManagerRole getRole() {
        return this.role;
    }

    public void setRole(ManagerRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getProjectIds() {
        return this.projectIds;
    }

    public void setProjectIds(String projectIds) {
        this.projectIds = projectIds;
    }

    public LocalDateTime getRegDatetime() {
        return this.regDatetime;
    }

    public void setRegDatetime(LocalDateTime regDatetime) {
        this.regDatetime = regDatetime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Manager other = (Manager) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        }
        else if (!username.equals(other.username))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        }
        else if (!password.equals(other.password))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        }
        else if (!description.equals(other.description))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        }
        else if (!role.equals(other.role))
            return false;
        if (active == null) {
            if (other.active != null)
                return false;
        }
        else if (!active.equals(other.active))
            return false;
        if (projectIds == null) {
            if (other.projectIds != null)
                return false;
        }
        else if (!projectIds.equals(other.projectIds))
            return false;
        if (regDatetime == null) {
            if (other.regDatetime != null)
                return false;
        }
        else if (!regDatetime.equals(other.regDatetime))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
        result = prime * result + ((this.password == null) ? 0 : this.password.hashCode());
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.role == null) ? 0 : this.role.hashCode());
        result = prime * result + ((this.active == null) ? 0 : this.active.hashCode());
        result = prime * result + ((this.projectIds == null) ? 0 : this.projectIds.hashCode());
        result = prime * result + ((this.regDatetime == null) ? 0 : this.regDatetime.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Manager (");

        sb.append(id);
        sb.append(", ").append(username);
        sb.append(", ").append(password);
        sb.append(", ").append(description);
        sb.append(", ").append(role);
        sb.append(", ").append(active);
        sb.append(", ").append(projectIds);
        sb.append(", ").append(regDatetime);

        sb.append(")");
        return sb.toString();
    }
}
