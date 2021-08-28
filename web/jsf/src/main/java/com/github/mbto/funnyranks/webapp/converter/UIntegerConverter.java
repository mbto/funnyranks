package com.github.mbto.funnyranks.webapp.converter;

import org.jooq.types.UInteger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

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