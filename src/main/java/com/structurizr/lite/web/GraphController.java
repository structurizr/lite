package com.structurizr.lite.web;


import com.structurizr.lite.Configuration;
import com.structurizr.lite.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraphController extends AbstractController {

    @RequestMapping(value = "/workspace/explore/graph", method = RequestMethod.GET)
    public String showGraph(@RequestParam(required = false, defaultValue = "") String view, ModelMap model) {
        return showGraph(1, view, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore/graph", method = RequestMethod.GET)
    public String showGraph(@PathVariable("workspaceId") long workspaceId,
                            @RequestParam(required = false, defaultValue = "") String view,
                            ModelMap model) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, "Structurizr Lite", true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", calculateUrlPrefix(workspaceId));
        model.addAttribute("type", "graph");
        model.addAttribute("view", view);

        return "graph";
    }

}