package com.github.mbto.funnyranks.webapp.converter;

import org.jooq.types.UInteger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = UInteger.class)
public class UIntegerConverter implements Converter<UInteger> {
    @Override
    public UInteger getAsObject(FacesContext context, UIComponent component, String value) {
        return UInteger.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, UInteger value) {
        return value.toString();
    }
}