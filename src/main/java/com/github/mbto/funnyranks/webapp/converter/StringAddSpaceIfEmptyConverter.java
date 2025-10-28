package com.github.mbto.funnyranks.webapp.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Named;

@FacesConverter(value = "stringAddSpaceIfEmptyConverter")
@Named
public class StringAddSpaceIfEmptyConverter implements Converter<String> {
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null)
            return " ";
        value = value.trim();
        return value.isEmpty() ? " " : value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return value == null ? "" : value.trim();
    }
}