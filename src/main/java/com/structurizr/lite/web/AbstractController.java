package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceComponent;
import com.structurizr.lite.util.RandomGuidGenerator;
import com.structurizr.lite.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.TimeZone;

public abstract class AbstractController {

    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    private static final String REFERER_POLICY_HEADER = "Referrer-Policy";
    private static final String REFERER_POLICY_VALUE = "strict-origin-when-cross-origin";
    private static final String SCRIPT_NONCE_ATTRIBUTE = "scriptNonce";

    protected WorkspaceComponent workspaceComponent;

    @ModelAttribute("structurizrConfiguration")
    public Configuration getConfiguration() {
        return Configuration.getInstance();
    }

    @ModelAttribute
    protected void addSecurityHeaders(HttpServletResponse response, ModelMap model) {
        response.addHeader(REFERER_POLICY_HEADER, REFERER_POLICY_VALUE);

        String nonce = Base64.getEncoder().encodeToString(new RandomGuidGenerator().generate().getBytes(StandardCharsets.UTF_8));
        model.addAttribute(SCRIPT_NONCE_ATTRIBUTE, nonce);

        response.addHeader(CONTENT_SECURITY_POLICY_HEADER, String.format("script-src 'self' 'nonce-%s'", nonce));
    }

    @ModelAttribute
    protected void addXFrameOptionsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("X-Frame-Options", "deny");
    }

    protected void addCommonAttributes(ModelMap model, String pageTitle, boolean showHeaderAndFooter) {
        model.addAttribute("timeZone", TimeZone.getDefault().getID());
        model.addAttribute("showHeader", showHeaderAndFooter);
        model.addAttribute("showFooter", showHeaderAndFooter);
        model.addAttribute("version", new Version());

        if (pageTitle == null || pageTitle.trim().length() == 0) {
            model.addAttribute("pageTitle", "Structurizr");
        } else {
            model.addAttribute("pageTitle", "Structurizr - " + pageTitle);
        }
    }

    protected String calculateUrlPrefix(long workspaceId) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return "/workspace";
        } else {
            return "/workspace/" + workspaceId;
        }
    }

    protected String show404Page(ModelMap model) {
        addCommonAttributes(model, "Not found", true);

        return "404";
    }

    protected String show500Page(ModelMap model) {
        addCommonAttributes(model, "Error", true);

        return "500";
    }

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

}