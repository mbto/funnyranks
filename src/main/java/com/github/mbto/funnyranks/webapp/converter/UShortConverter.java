package com.github.mbto.funnyranks.webapp.converter;

import org.jooq.types.UShort;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = UShort.class)
public class UShortConverter implements Converter<UShort> {
    @Override
    public UShort getAsObject(FacesContext context, UIComponent component, String value) {
        return value != null ? UShort.valueOf(value) : null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, UShort value) {
        return value != null ? value.toString() : "";
    }
}