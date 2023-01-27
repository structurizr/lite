package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DecisionsController extends AbstractController {

    @RequestMapping(value = "/workspace/decisions", method = RequestMethod.GET)
    public String showDecisionsForWorkspace(ModelMap model) {
        model.addAttribute("scope", "*");

        return showDecisions(model);
    }

    @RequestMapping(value = "/workspace/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showDecisionsForSoftwareSystem(
            @PathVariable(value="softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        model.addAttribute("scope", HtmlUtils.filterHtml(softwareSystem));

        return showDecisions(model);
    }

    @RequestMapping(value = "/workspace/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showDecisionsForContainer(
            @PathVariable(value="softwareSystem") String softwareSystem,
            @PathVariable(value="container") String container,
            ModelMap model
    ) {
        model.addAttribute("scope", HtmlUtils.filterHtml(softwareSystem) + "/" + HtmlUtils.filterHtml(container));

        return showDecisions(model);
    }

    public String showDecisions(ModelMap model) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData();
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, "Structurizr Lite", true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", "/workspace");
        model.addAttribute("autoRefreshInterval", Configuration.getInstance().getAutoRefreshInterval());
        model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());

        return "decisions";
    }

}