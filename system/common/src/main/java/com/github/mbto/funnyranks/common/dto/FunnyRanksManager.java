package com.github.mbto.funnyranks.common.dto;

import com.github.mbto.funnyranks.common.model.funnyranks.tables.pojos.Manager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.TreeSet;

import static com.github.mbto.funnyranks.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole.broker;
import static com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole.project;

public class FunnyRanksManager extends Manager implements UserDetails, CredentialsContainer {
    @Getter
    @Setter
    private Set<UInteger> availableProjectIds;
    @Getter
    private Set<GrantedAuthority> authorities;
    private boolean additionalFieldsChanged;

    public void setupAdditionals() {
        if (getProjectIds() != null) {
            JsonArray jsonArray = new Gson().fromJson(getProjectIds(), JsonArray.class);
            for (JsonElement jsonElement : jsonArray) {
                if (availableProjectIds == null)
                    availableProjectIds = new TreeSet<>();
                availableProjectIds.add(UInteger.valueOf(jsonElement.getAsLong()));
            }
        }
        this.authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + getRole().getLiteral()));
        additionalFieldsChanged = true;
    }

    public void applyFromAdditionals() {
        if (!additionalFieldsChanged)
            return;
        if (availableProjectIds == null) {
            setProjectIds(null);
            return;
        }
        JsonArray jsonArray = null;
        for (UInteger selectedProjectId : availableProjectIds) {
            if (jsonArray == null)
                jsonArray = new JsonArray();
            jsonArray.add(selectedProjectId.longValue());
        }
        if (jsonArray == null) {
            setProjectIds(null);
            return;
        }
        setProjectIds(new Gson().toJson(jsonArray));
    }

    public boolean canManageBroker() {
        return getRole() == broker;
    }

    public boolean canManageProject(UInteger projectId) {
        return getRole() == broker || (getRole() == project
                && availableProjectIds != null
                && availableProjectIds.contains(projectId));
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getActive();
    }

    @Override
    public void eraseCredentials() {
        setPassword(null);
    }

    @Override
    public String toString() {
        return "Username: " + getUsername() + "[" + getId() + "]; " +
                "Role: " + getRole().getLiteral() + "; " +
                "Authorities: " + authorities + "; " +
                "Enabled: " + isEnabled() + "; " +
                "Available project ids: " + availableProjectIds + "; " +
                "Reg dateTime: " + getRegDatetime().format(YYYYMMDD_HHMMSS_PATTERN) + "; " +
                "AccountNonExpired: " + isAccountNonExpired() + "; " +
                "CredentialsNonExpired: " + isCredentialsNonExpired() + "; " +
                "AccountNonLocked: " + isAccountNonLocked() + "; "
                ;
    }

    public String toStringSimple() {
        return getUsername() + "[" + getId() + "]"
                + ", role: " + getRole().getLiteral()
                + ", authorities: " + (authorities != null ? authorities : "No authorities");
    }
}
