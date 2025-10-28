package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = ProjectLanguage.class)
public class ProjectLanguageConverter implements Converter<ProjectLanguage> {
    @Override
    public ProjectLanguage getAsObject(FacesContext context, UIComponent component, String value) {
        return ProjectLanguage.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProjectLanguage value) {
        return value.name();
    }
}