package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ExploreController extends AbstractController {

    @RequestMapping(value = "/workspace/explore", method = RequestMethod.GET)
    public String showExplorePage(ModelMap model) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return showExplorePage(1, model);
        } else {
            return "redirect:/workspace/1/explore";
        }
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore", method = RequestMethod.GET)
    public String showExplorePage(@PathVariable("workspaceId") long workspaceId,
                                  ModelMap model) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, "Structurizr Lite", true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", calculateUrlPrefix(workspaceId));
        model.addAttribute("thumbnailUrl", "/workspace/" + workspaceId + "/images/");

        return "explore";
    }

}