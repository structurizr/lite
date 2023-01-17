package com.structurizr.lite.web;


import com.structurizr.Workspace;
import com.structurizr.lite.component.workspace.NoWorkspaceFoundException;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController extends AbstractController {

    private static final Log log = LogFactory.getLog(HomeController.class);

    @RequestMapping(value = { "/", "/workspace" }, method = RequestMethod.GET)
    public String showDefaultPage(ModelMap model) {
        try {
            Workspace workspace = workspaceComponent.getWorkspace();

            if (hasDocumentation(workspace)) {
                return "redirect:/workspace/documentation";
            } else if (hasDecisions(workspace)) {
                return "redirect:/workspace/decisions";
            } else {
                return "redirect:/workspace/diagrams";
            }
        } catch (NoWorkspaceFoundException e) {
            log.error(e.getMessage());
            model.addAttribute("error", e.getMessage());

            addCommonAttributes(model, "Structurizr Lite", true);
            return "error";
        }
    }

    private boolean hasDocumentation(Workspace workspace) {
        if (!workspace.getDocumentation().getSections().isEmpty()) {
            return true;
        }

        // check for software system level documentation
        for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
            if (!softwareSystem.getDocumentation().getSections().isEmpty()) {
                return true;
            }

            // and container level documentation
            for (Container container : softwareSystem.getContainers()) {
                if (!container.getDocumentation().getSections().isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasDecisions(Workspace workspace) {
        if (!workspace.getDocumentation().getDecisions().isEmpty()) {
            return true;
        }

        // check for software system level decisions
        for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
            if (!softwareSystem.getDocumentation().getDecisions().isEmpty()) {
                return true;
            }

            // and container level decisions
            for (Container container : softwareSystem.getContainers()) {
                if (!container.getDocumentation().getDecisions().isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

}