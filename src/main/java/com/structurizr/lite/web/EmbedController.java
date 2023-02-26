package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import com.structurizr.lite.util.JsonUtils;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class EmbedController extends AbstractController {

    @Override
    protected void addXFrameOptionsHeader(HttpServletRequest request, HttpServletResponse response) {
        // do nothing ... this page is supposed to be iframe'd
    }

    @RequestMapping(value = "/workspace/embed")
    public String embedDiagrams(
            @RequestParam(value = "diagram", required = false) String diagramIdentifier,
            @RequestParam(required = false) boolean diagramSelector,
            @RequestParam(required = false, defaultValue = "") String iframe,
            @RequestParam(required = false) String perspective,
            ModelMap model) throws Exception {

        diagramIdentifier = HtmlUtils.filterHtml(diagramIdentifier);
        diagramIdentifier = HtmlUtils.escapeQuoteCharacters(diagramIdentifier);
        iframe = HtmlUtils.filterHtml(iframe);
        perspective = HtmlUtils.filterHtml(perspective);

        if (diagramIdentifier != null && diagramIdentifier.length() > 0) {
            model.addAttribute("diagramIdentifier", diagramIdentifier);
        }

        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData();
        workspaceMetaData.setEditable(false);

        addCommonAttributes(model, "Structurizr Lite", false);
        model.addAttribute("workspace", workspaceMetaData);

        Workspace workspace = workspaceComponent.getWorkspace();
        String json = WorkspaceUtils.toJson(workspace, false);
        model.addAttribute("workspaceAsJson", JsonUtils.base64(json));

        model.addAttribute("urlPrefix", "/workspace");
        model.addAttribute("urlSuffix", "");

        model.addAttribute("showToolbar", diagramSelector);
        model.addAttribute("showDiagramSelector", diagramSelector);
        model.addAttribute("embed", true);
        model.addAttribute("iframe", iframe);
        model.addAttribute("perspective", perspective);
        model.addAttribute("publishThumbnails", true);

        return "diagrams";
    }

    @RequestMapping(value = "/embed", method = RequestMethod.GET)
    public String embedFromParent(@RequestParam(required = false, defaultValue = "0") long workspace,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String view,
                                  @RequestParam(required = false) String perspective,
                                  @RequestParam(required = false, defaultValue = "false") boolean editable,
                                  @RequestParam(required = false, defaultValue = "") String iframe,
                                  @RequestParam(required = false, defaultValue = "true") boolean fullscreen,
                                  @RequestParam(required = false) String urlPrefix,
                                  @RequestParam(required = false) String urlSuffix,
                                  ModelMap model) {

        type = HtmlUtils.filterHtml(type);
        view = HtmlUtils.filterHtml(view);
        view = HtmlUtils.escapeQuoteCharacters(view);
        perspective = HtmlUtils.filterHtml(perspective);
        iframe = HtmlUtils.filterHtml(iframe);

        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData();
        workspaceMetaData.setName("Embedded workspace");
        workspaceMetaData.setEditable(editable);

        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("loadWorkspaceFromParent", true);
        model.addAttribute("embed", true);
        model.addAttribute("iframe", iframe);
        addCommonAttributes(model, "", false);

        model.addAttribute("urlPrefix", "/workspace");

        if (!StringUtils.isNullOrEmpty(perspective)) {
            if (StringUtils.isNullOrEmpty(urlSuffix)) {
                urlSuffix = "?perspective=" + perspective;
            } else {
                urlSuffix = urlSuffix + "&perspective=" + perspective;
            }
        }
        model.addAttribute("urlSuffix", urlSuffix);

        if ("graph".equals(type)) {
            model.addAttribute("view", view);

            return "graph";
        } else if ("tree".equals(type)) {
            model.addAttribute("view", view);

            return "tree";
        } else {
            if (!StringUtils.isNullOrEmpty(view)) {
                model.addAttribute("diagramIdentifier", view);
            }

            model.addAttribute("publishThumbnails", true);

            model.addAttribute("showToolbar", editable);
            model.addAttribute("showDiagramSelector", false);
            model.addAttribute("perspective", perspective);
            model.addAttribute("fullscreen", fullscreen);

            return "diagrams";
        }
    }

}