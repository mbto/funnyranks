package com.github.mbto.funnyranks.webapp.validator;

import org.apache.commons.lang3.StringUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import static com.github.mbto.funnyranks.common.Constants.MYSQL_NAMING_PATTERN;

@FacesValidator(value = "mySQLNamingValidator")
public class MySQLNamingValidator implements Validator<String> {
    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (StringUtils.isBlank(value) || !MYSQL_NAMING_PATTERN.matcher(value).matches())
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Invalid value '" + value + "'", ""));
    }
}