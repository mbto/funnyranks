package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = ManagerRole.class)
public class ManagerRoleConverter implements Converter<ManagerRole> {
    @Override
    public ManagerRole getAsObject(FacesContext context, UIComponent component, String value) {
        return ManagerRole.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ManagerRole value) {
        return value.name();
    }
}