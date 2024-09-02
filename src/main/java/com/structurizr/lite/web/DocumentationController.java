package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class DocumentationController extends AbstractController {

    @RequestMapping(value = "/workspace/documentation", method = RequestMethod.GET)
    public String showDocumentationForWorkspace(
            ModelMap model) {
        return showDocumentationForWorkspace(1, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation", method = RequestMethod.GET)
    public String showDocumentationForWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model) {
        String scope = "*";
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDocumentation(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showDocumentationForSoftwareSystem(
            @PathVariable(value="softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showDocumentationForSoftwareSystem(1, softwareSystem, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showDocumentationForSoftwareSystem(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        String scope = softwareSystem;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDocumentation(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showDocumentationForContainer(
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            ModelMap model
    ) {
        return showDocumentationForContainer(1, softwareSystem, container, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showDocumentationForContainer(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            ModelMap model
    ) {
        String scope = softwareSystem + "/" + container;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDocumentation(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showDocumentationForComponent(
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            @PathVariable(value="component") String component,
            ModelMap model
    ) {
        return showDocumentationForComponent(1, softwareSystem, container, component, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showDocumentationForComponent(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            @PathVariable(value="component") String component,
            ModelMap model
    ) {
        String scope = softwareSystem + "/" + container + "/" + component;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDocumentation(workspaceId, model);
    }

    private String showDocumentation(long workspaceId, ModelMap model) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, "Structurizr Lite", true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", calculateUrlPrefix(workspaceId));
        model.addAttribute("autoRefreshInterval", Configuration.getInstance().getAutoRefreshInterval());
        model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());

        return "documentation";
    }

}