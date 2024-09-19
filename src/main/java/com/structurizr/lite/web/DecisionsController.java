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

/**
 * Provides access to architecture decision records (ADRs) defined within a workspace.
 */
@Controller
public class DecisionsController extends AbstractController {

    @RequestMapping(value = "/workspace/decisions", method = RequestMethod.GET)
    public String showDecisionsForWorkspace(ModelMap model) {
        return showDecisionsForWorkspace(1, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions", method = RequestMethod.GET)
    public String showDecisionsForWorkspace(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model) {
        String scope = "*";
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDecisions(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showDecisionsForSoftwareSystem(
            @PathVariable(value="softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showDecisionsForSoftwareSystem(1, softwareSystem, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showDecisionsForSoftwareSystem(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        String scope = softwareSystem;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDecisions(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showDecisionsForContainer(
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            ModelMap model
    ) {
        return showDecisionsForContainer(1, softwareSystem, container, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showDecisionsForContainer(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            ModelMap model
    ) {
        String scope = softwareSystem + "/" + container;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDecisions(workspaceId, model);
    }

    @RequestMapping(value = "/workspace/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showDecisionsForComponent(
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            @PathVariable(value="component") String component,
            ModelMap model
    ) {
        return showDecisionsForComponent(1, softwareSystem, container, component, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showDecisionsForComponent(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            @PathVariable(value="component") String component,
            ModelMap model
    ) {
        String scope = softwareSystem + "/" + container + "/" + component;
        model.addAttribute("scope", Base64.getEncoder().encodeToString(scope.getBytes(StandardCharsets.UTF_8)));

        return showDecisions(workspaceId, model);
    }

    public String showDecisions(long workspaceId, ModelMap model) {
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

        return "decisions";
    }

}