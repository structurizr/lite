package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ModelController extends AbstractController {

    @RequestMapping(value = "/workspace/explore/model", method = RequestMethod.GET)
    public String showModel(
            ModelMap model) {
        return showModel(1, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore/model", method = RequestMethod.GET)
    public String showModel(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model) {

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

        return "model";
    }

}