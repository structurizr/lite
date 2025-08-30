package com.structurizr.lite.web;

import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.workspace.WorkspaceComponentException;
import com.structurizr.lite.domain.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import com.structurizr.lite.util.Image;
import com.structurizr.view.PaperSize;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Provides access to the diagrams defined within a workspace.
 */
@Controller
public class DiagramsController extends AbstractController {

    private static final Log log = LogFactory.getLog(DiagramsController.class);

    @RequestMapping(value = "/workspace/diagrams", method = RequestMethod.GET)
    public String showDiagrams(ModelMap model,
                               @RequestParam(required = false) String perspective) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return showDiagrams(1, model, perspective);
        } else {
            return "redirect:/workspace/1/diagrams";
        }
    }

    @RequestMapping(value = "/workspace/{workspaceId}/diagrams", method = RequestMethod.GET)
    public String showDiagrams(@PathVariable("workspaceId") long workspaceId,
                               ModelMap model,
                               @RequestParam(required = false) String perspective) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspaceId);
        workspaceMetaData.setEditable(Configuration.getInstance().isEditable());
        workspaceMetaData.setApiKey(Configuration.getInstance().getApiKey());
        workspaceMetaData.setApiSecret(Configuration.getInstance().getApiSecret());

        addCommonAttributes(model, "Structurizr Lite", false);
        model.addAttribute("workspace", workspaceMetaData);
        model.addAttribute("urlPrefix", calculateUrlPrefix(workspaceId));
        model.addAttribute("thumbnailUrl", "/workspace/" + workspaceId + "/images/");

        model.addAttribute("showToolbar", true);
        model.addAttribute("embed", false);
        if (workspaceMetaData.isEditable()) {
            model.addAttribute("paperSizes", PaperSize.getOrderedPaperSizes());
        }
        model.addAttribute("publishThumbnails", true);
        model.addAttribute("quickNavigationPath", "diagrams");
        model.addAttribute("perspective", HtmlUtils.filterHtml(perspective));
        model.addAttribute("autoSaveInterval", Configuration.getInstance().getAutoSaveInterval());
        model.addAttribute("autoRefreshInterval", Configuration.getInstance().getAutoRefreshInterval());
        model.addAttribute("autoRefreshLastModifiedDate", workspaceComponent.getLastModifiedDate());
        model.addAttribute("inspectionSummary", true);

        return "diagrams";
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    public void optionsWorkspaceImage(@PathVariable("workspaceId") long workspaceId,
                                      @PathVariable("filename") String filename,
                                      HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    private void addAccessControlAllowHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "accept, origin, " + HttpHeaders.CONTENT_TYPE);
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT");
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspaceImage(@PathVariable("workspaceId") long workspaceId,
                                                       @PathVariable("filename")String filename,
                                                       @RequestBody String imageAsBase64EncodedDataUri,
                                                       @ModelAttribute("remoteIpAddress") String ipAddress) {

        try {
            if (workspaceComponent.putImage(workspaceId, filename, imageAsBase64EncodedDataUri)) {
                return new ApiResponse("OK");
            } else {
                throw new ApiException("Failed to save image");
            }
        } catch (WorkspaceComponentException e) {
            e.printStackTrace();
            throw new ApiException("Failed to save image");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/images/{*filename}", method = RequestMethod.GET)
    public ResponseEntity getImage(@PathVariable("filename") String filename,
                                   HttpServletResponse response) {
        return getImage(1, filename, response);
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/images/{*filename}", method = RequestMethod.GET)
    public ResponseEntity getImage(@PathVariable("workspaceId") long workspaceId,
                             @PathVariable("filename") String filename,
                             HttpServletResponse response) {
        filename = HtmlUtils.filterHtml(filename);

        try {
            Image image = workspaceComponent.getImage(workspaceId, filename);
            if (image != null) {
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.valueOf(image.getContentType()))
                        .body(new InputStreamResource(image.getInputStream()) {
                            @Override
                            public long contentLength() {
                                return image.getContentLength();
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e);
        }

        response.setStatus(404);
        return null;
    }

}