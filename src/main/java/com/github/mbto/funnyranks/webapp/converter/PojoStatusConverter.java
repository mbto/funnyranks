package com.github.mbto.funnyranks.webapp.converter;

import com.github.mbto.funnyranks.webapp.PojoStatus;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass = PojoStatus.class)
public class PojoStatusConverter implements Converter<PojoStatus> {
    @Override
    public PojoStatus getAsObject(FacesContext context, UIComponent component, String value) {
        return PojoStatus.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PojoStatus value) {
        return value.name();
    }
}