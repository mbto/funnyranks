package com.github.mbto.funnyranks.webapp;

import com.github.mbto.funnyranks.common.model.funnyranks.enums.ManagerRole;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectDatabaseServerTimezone;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectLanguage;
import com.github.mbto.funnyranks.common.model.funnyranks.enums.ProjectMergeType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.Dependent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Arrays;

@Dependent
@Named
@Slf4j
public class DependentUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private static final SelectItem[] AVAILABLE_TIME_ZONES = Arrays.stream(ProjectDatabaseServerTimezone.values())
            .map(value -> new SelectItem(value, value.getLiteral()))
            .toArray(SelectItem[]::new);

    @Getter
    private static final SelectItem[] AVAILABLE_LANGUAGES = Arrays.stream(ProjectLanguage.values())
            .map(value -> new SelectItem(value, value.getLiteral()))
            .toArray(SelectItem[]::new);

    @Getter
    private static final SelectItem[] AVAILABLE_MERGE_TYPES = Arrays.stream(ProjectMergeType.values())
            .map(value -> new SelectItem(value, value.getLiteral()))
            .toArray(SelectItem[]::new);

    @Getter
    private static final SelectItem[] AVAILABLE_MANAGER_ROLES = Arrays.stream(ManagerRole.values())
            .map(value -> new SelectItem(value, value.getLiteral()))
            .toArray(SelectItem[]::new);

    // showPortsForm:portsTblId_instantSelectedRowKey 7
    // showPortsForm:portsTblId_selection 7
    // listener="#{redirectByEvent.onRowSelect('showPortsForm','portsTblId','portsByProject','projectId')}"
    public boolean trySendRedirect(String formId, String dataTableId, String viewName, String paramName) {
        String paramValue = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap().get(formId + ":" + dataTableId + "_selection");

        if (!StringUtils.isBlank(paramValue)) {
            String redirectUrl = viewName + "?" + paramName + "=" + paramValue;
            return sendRedirect(redirectUrl);
        }
        return false;
    }

    public boolean sendRedirect(String redirectUrl) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext exCtx = fc.getExternalContext();
        redirectUrl = exCtx.getRequestContextPath() + "/" + redirectUrl;
        if (log.isDebugEnabled())
            log.debug("\nredirectUrl=" + redirectUrl);
        try {
            exCtx.redirect(redirectUrl);
            return true;
        } catch (Throwable e) {
            String msg = "Failed redirect to '" + redirectUrl + "'";
            log.warn(msg, e);
//            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg, ""));
            return false;
        } finally {
            fc.responseComplete();
        }
    }
//    public String declension(long n, String o1, String o2, String o3) {
//        return ProjectUtils.declension(n, o1, o2, o3);
//    }
}