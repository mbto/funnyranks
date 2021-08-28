package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectDatabaseServerTimezone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

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