package com.github.mbto.funnyranks.webapp.converter;

import org.jooq.types.UShort;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

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