package com.github.mbto.funnyranks.webapp;

import com.github.mbto.funnyranks.common.dto.FunnyRanksManager;
import org.apache.commons.lang3.StringUtils;
import org.jooq.types.UInteger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.servlet.http.HttpServletRequest;

import static jakarta.faces.application.FacesMessage.SEVERITY_WARN;

public class WebUtils {
    public static ValidatorException makeValidatorException(String value, String details) {
        return new ValidatorException(new FacesMessage(SEVERITY_WARN,
                "Failed validation value '" + value + "'",
                details));
    }

    public static FunnyRanksManager canManageProject(UInteger projectId, boolean addFailedMsg) {
        return canManageProject(projectId, addFailedMsg, "msgs");
    }

    public static FunnyRanksManager canManageProject(UInteger projectId, boolean addFailedMsg, String msgComponentId) {
        FunnyRanksManager funnyRanksManager = extractFunnyRanksManager(addFailedMsg, msgComponentId);
        if (funnyRanksManager == null || !funnyRanksManager.canManageProject(projectId)) {
            if (addFailedMsg) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(msgComponentId, new FacesMessage(SEVERITY_WARN, "You can't manage this project", ""));
            }
            return null;
        }
        return funnyRanksManager;
    }

    public static FunnyRanksManager canManageBroker() {
        return canManageBroker(false, null);
    }

    public static FunnyRanksManager canManageBroker(boolean addFailedMsg) {
        return canManageBroker(addFailedMsg, "msgs");
    }

    public static FunnyRanksManager canManageBroker(boolean addFailedMsg, String msgComponentId) {
        FunnyRanksManager funnyRanksManager = extractFunnyRanksManager(addFailedMsg, msgComponentId);
        if (funnyRanksManager == null || !funnyRanksManager.canManageBroker()) {
            if (addFailedMsg) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(msgComponentId, new FacesMessage(SEVERITY_WARN, "You can't manage brokers", ""));
            }
            return null;
        }
        return funnyRanksManager;
    }

    public static FunnyRanksManager extractFunnyRanksManager() {
        return extractFunnyRanksManager(false, null);
    }

    public static FunnyRanksManager extractFunnyRanksManager(boolean addFailedMsg) {
        return extractFunnyRanksManager(addFailedMsg, "msgs");
    }

    public static FunnyRanksManager extractFunnyRanksManager(boolean addFailedMsg, String msgComponentId) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            if (addFailedMsg)
                fc.addMessage(msgComponentId, new FacesMessage(SEVERITY_WARN, "Yours authentication is empty", ""));
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof FunnyRanksManager)) {
            if (addFailedMsg)
                fc.addMessage(msgComponentId, new FacesMessage(SEVERITY_WARN, "Yours principal identity is empty", ""));
            return null;
        }
        return (FunnyRanksManager) principal;
    }

    public static String getManagerInfo() {
        return getManagerInfo(extractFunnyRanksManager());
    }

    public static String getManagerInfo(FunnyRanksManager funnyRanksManager) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        String ip = ec.getRequestHeaderMap().get("X-Real-IP"); // nginx
        if(StringUtils.isBlank(ip)) {
            HttpServletRequest req = (HttpServletRequest) ec.getRequest();
            ip = req.getRemoteAddr();
        }
        return "(" + (funnyRanksManager != null ? ("Manager: " + funnyRanksManager.toStringSimple()) : "No manager")
               + ", IP: " + ip + ")";
    }
}