package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = ProjectMergeType.class)
public class ProjectMergeTypeConverter implements Converter<ProjectMergeType> {
    @Override
    public ProjectMergeType getAsObject(FacesContext context, UIComponent component, String value) {
        return ProjectMergeType.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProjectMergeType value) {
        return value.name();
    }
}