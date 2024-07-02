package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController extends AbstractController {

    private static final Log log = LogFactory.getLog(HomeController.class);

    private static final String SORT_DATE = "date";
    private static final String SORT_NAME = "name";

    private static final String DEFAULT_PAGE_NUMBER = "1";
    private static final String DEFAULT_PAGE_SIZE = "" + PaginatedWorkspaceList.DEFAULT_PAGE_SIZE;

    @RequestMapping(value = { "/" }, method = RequestMethod.GET)
    public String showHomePage(
            @RequestParam(required = false, defaultValue = SORT_NAME) String sort,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            ModelMap model) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return showWorkspace(1, model);
        } else {
            List<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
            sort = determineSort(sort);
            workspaces = sortAndPaginate(new ArrayList<>(workspaces), sort, pageNumber, pageSize, model);

            model.addAttribute("workspaces", workspaces);
            model.addAttribute("numberOfWorkspaces", workspaces.size());

            model.addAttribute("sort", sort);
            addCommonAttributes(model, "Structurizr Lite", true);

            return "home";
        }
    }

    private List<WorkspaceMetaData> sortAndPaginate(List<WorkspaceMetaData> workspaces, String sort, int pageNumber, int pageSize, ModelMap model) {
        if (SORT_DATE.equals(sort)) {
            workspaces.sort((wmd1, wmd2) -> wmd2.getLastModifiedDate().compareTo(wmd1.getLastModifiedDate()));
        } else {
            workspaces.sort(Comparator.comparing(wmd -> wmd.getName().toLowerCase()));
        }

        if (workspaces.isEmpty() || pageSize >= workspaces.size()) {
            return workspaces;
        } else {
            PaginatedWorkspaceList paginatedWorkspaceList = new PaginatedWorkspaceList(workspaces, pageNumber, pageSize);

            model.addAttribute("pageNumber", paginatedWorkspaceList.getPageNumber());
            if (paginatedWorkspaceList.hasPreviousPage()) {
                model.addAttribute("previousPage", pageNumber - 1);
            }
            if (paginatedWorkspaceList.hasNextPage()) {
                model.addAttribute("nextPage", pageNumber + 1);
            }

            model.addAttribute("maxPage", paginatedWorkspaceList.getMaxPage());
            model.addAttribute("pageSize", paginatedWorkspaceList.getPageSize());

            return paginatedWorkspaceList.getWorkspaces();
        }
    }

    private String determineSort(String sort) {
        sort = HtmlUtils.filterHtml(sort);

        if (!StringUtils.isNullOrEmpty(sort) && sort.trim().equals(SORT_DATE)) {
            sort = SORT_DATE;
        } else {
            sort = SORT_NAME;
        }

        return sort;
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