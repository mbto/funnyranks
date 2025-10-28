package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectDatabaseServerTimezone;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = ProjectDatabaseServerTimezone.class)
public class ProjectDatabaseServerTimezoneConverter implements Converter<ProjectDatabaseServerTimezone> {
    @Override
    public ProjectDatabaseServerTimezone getAsObject(FacesContext context, UIComponent component, String value) {
        return ProjectDatabaseServerTimezone.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProjectDatabaseServerTimezone value) {
        return value.name();
    }
}