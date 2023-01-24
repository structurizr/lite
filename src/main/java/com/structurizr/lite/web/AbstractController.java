package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceComponent;
import com.structurizr.lite.util.RandomGuidGenerator;
import com.structurizr.lite.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.TimeZone;

public abstract class AbstractController {

    protected WorkspaceComponent workspaceComponent;

    @ModelAttribute("structurizrConfiguration")
    public Configuration getConfiguration(HttpServletRequest request) {
        Configuration configuration = Configuration.getInstance();
        if (configuration.getWebUrl() == null || configuration.getWebUrl().trim().length() == 0) {
            String url = request.getScheme()
                    + "://"
                    + request.getServerName()
                    + ":"
                    + request.getServerPort()
                    + request.getContextPath();

            configuration.setWebUrl(url);
        }

        return configuration;
    }

    @ModelAttribute
    protected void addSecurityHeaders(HttpServletResponse response, ModelMap model) {
        response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        String nonce = Base64.getEncoder().encodeToString(new RandomGuidGenerator().generate().getBytes(StandardCharsets.UTF_8));
        model.addAttribute("scriptNonce", nonce);
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