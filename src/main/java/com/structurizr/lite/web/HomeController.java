package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.lite.Configuration;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController extends AbstractController {

    private static final Log log = LogFactory.getLog(HomeController.class);

    @RequestMapping(value = { "/" }, method = RequestMethod.GET)
    public String showHomePage(ModelMap model) {
        return showWorkspace(1, model);
    }

    @RequestMapping(value = { "/workspace" }, method = RequestMethod.GET)
    public String showWorkspace(ModelMap model) {
        return showWorkspace(1, model);
    }

    @RequestMapping(value = { "/workspace/{workspaceId}" }, method = RequestMethod.GET)
    public String showWorkspace(@PathVariable("workspaceId") long workspaceId,
                                ModelMap model) {
        try {
            Workspace workspace = workspaceComponent.getWorkspace(workspaceId);
            if (workspace == null) {
                model.addAttribute("error", workspaceComponent.getError());

                addCommonAttributes(model, "Structurizr Lite", true);
                return "error";
            }

            if (hasDocumentation(workspace)) {
                if (Configuration.getInstance().isSingleWorkspace()) {
                    return "redirect:/workspace/documentation";
                } else {
                    return "redirect:/workspace/" + workspaceId + "/documentation";
                }
            } else if (hasDecisions(workspace)) {
                if (Configuration.getInstance().isSingleWorkspace()) {
                    return "redirect:/workspace/decisions";
                } else {
                    return "redirect:/workspace/" + workspaceId + "/decisions";
                }
            } else {
                if (Configuration.getInstance().isSingleWorkspace()) {
                    return "redirect:/workspace/diagrams";
                } else {
                    return "redirect:/workspace/" + workspaceId + "/diagrams";
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            model.addAttribute("error", e.getMessage());

            addCommonAttributes(model, "Structurizr Lite", true);
            return "error";
        }
    }

    private boolean hasDocumentation(Workspace workspace) {
        if (workspace.getDocumentation() != null && !workspace.getDocumentation().getSections().isEmpty()) {
            return true;
        }

        // check for software system level documentation
        for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
            if (softwareSystem.getDocumentation() != null && !softwareSystem.getDocumentation().getSections().isEmpty()) {
                return true;
            }

            // and container level documentation
            for (Container container : softwareSystem.getContainers()) {
                if (container.getDocumentation() != null && !container.getDocumentation().getSections().isEmpty()) {
                    return true;
                }

                // and component level documentation
                for (Component component : container.getComponents()) {
                    if (component.getDocumentation() != null && !component.getDocumentation().getSections().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean hasDecisions(Workspace workspace) {
        if (workspace.getDocumentation() != null && !workspace.getDocumentation().getDecisions().isEmpty()) {
            return true;
        }

        // check for software system level decisions
        for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
            if (softwareSystem.getDocumentation() != null && !softwareSystem.getDocumentation().getDecisions().isEmpty()) {
                return true;
            }

            // and container level decisions
            for (Container container : softwareSystem.getContainers()) {
                if (container.getDocumentation() != null && !container.getDocumentation().getDecisions().isEmpty()) {
                    return true;
                }

                // and component level decisions
                for (Component component : container.getComponents()) {
                    if (component.getDocumentation() != null && !component.getDocumentation().getDecisions().isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}