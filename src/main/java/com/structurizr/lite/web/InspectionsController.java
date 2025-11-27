package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.inspection.Inspector;
import com.structurizr.inspection.Severity;
import com.structurizr.inspection.Violation;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.domain.WorkspaceMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Comparator;
import java.util.List;

@Controller
public class InspectionsController extends AbstractController {

    @RequestMapping(value = "/workspace/inspections", method = RequestMethod.GET)
    public String showInspectionPage(ModelMap model) {
        return showInspectionPage(1, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/inspections", method = RequestMethod.GET)
    public String showInspectionPage(@PathVariable("workspaceId") long workspaceId,
                                     ModelMap model) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
        workspaceMetaData.setEditable(false);
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        Workspace workspace = workspaceComponent.getWorkspace(1, false);
        if (workspace == null) {
            model.addAttribute("error", workspaceComponent.getError());
        } else {
            Inspector inspector = new DefaultInspector(workspace);
            List<Violation> violations = inspector.getViolations();
            violations.sort(Comparator.comparing(Violation::getSeverity));
            model.addAttribute("violations", violations);
            model.addAttribute("numberOfInspections", inspector.getNumberOfInspections());
            model.addAttribute("numberOfViolations", violations.size());
            model.addAttribute("numberOfErrors", violations.stream().filter(r -> r.getSeverity() == Severity.ERROR).count());
            model.addAttribute("numberOfWarnings", violations.stream().filter(r -> r.getSeverity() == Severity.WARNING).count());
            model.addAttribute("numberOfInfos", violations.stream().filter(r -> r.getSeverity() == Severity.INFO).count());
            model.addAttribute("numberOfIgnores", violations.stream().filter(r -> r.getSeverity() == Severity.IGNORE).count());

            workspaceMetaData.setName(workspace.getName());
            workspaceMetaData.setDescription(workspace.getDescription());
        }

        addCommonAttributes(model, "Structurizr Lite", true);
        model.addAttribute("showFooter", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", calculateUrlPrefix(workspaceId));
        model.addAttribute("autoRefreshInterval", Configuration.getInstance().getAutoRefreshInterval());
        model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());

        return "inspections";
    }

}