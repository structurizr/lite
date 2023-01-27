package com.structurizr.lite.web;


import com.structurizr.Workspace;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraphController extends AbstractController {

    @RequestMapping(value = "/workspace/explore/graph", method = RequestMethod.GET)
    public String showDocumentation(@RequestParam(required = false, defaultValue = "") String view, ModelMap model) {
        Workspace workspace = workspaceComponent.getWorkspace();
        if (workspace == null) {
            return "redirect:/";
        }

        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData();
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, workspace.getName(), true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", "/workspace");

        model.addAttribute("type", "graph");
        model.addAttribute("view", view);

        model.addAttribute("showDiagramsNavigationLink", !workspace.getViews().isEmpty());
        model.addAttribute("showDocumentationNavigationLink", !workspace.getDocumentation().getSections().isEmpty());
        model.addAttribute("showDecisionsNavigationLink", !workspace.getDocumentation().getDecisions().isEmpty());

        return "graph";
    }
}