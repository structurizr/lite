package com.structurizr.lite.web;

import com.structurizr.Workspace;
import com.structurizr.graphviz.GraphvizAutomaticLayout;
import com.structurizr.graphviz.RankDirection;
import com.structurizr.lite.Configuration;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class GraphvizController {

    private static int COUNTER = 0;

    @PostMapping(value = "/graphviz", consumes = "application/json", produces = "application/json; charset=UTF-8")
    public String post(@RequestBody String json,
                       @RequestParam(required = false) String view,
                       @RequestParam(required = false, defaultValue = "TB") String rankDirection,
                       @RequestParam(required = false, defaultValue = "true") boolean resizePaper,
                       @RequestParam(required = false, defaultValue = "300") int rankSeparation,
                       @RequestParam(required = false, defaultValue = "300") int nodeSeparation,
                       @RequestParam(required = false, defaultValue = "400") int margin) {
        try {
            Workspace workspace = WorkspaceUtils.fromJson(json);
            try {
                ThemeUtils.loadThemes(workspace);
            } catch (Exception e) {
                e.printStackTrace();
            }

            File graphvizPath = new File(Configuration.getInstance().getWorkDirectory(), "graphviz");
            graphvizPath.mkdirs();
            graphvizPath.deleteOnExit();

            File path = new File(graphvizPath, "" + ++COUNTER);
            path.mkdirs();

            GraphvizAutomaticLayout graphviz = new GraphvizAutomaticLayout(path);
            graphviz.setRankDirection(findRankDirection(rankDirection));
            graphviz.setChangePaperSize(resizePaper);
            graphviz.setRankSeparation(rankSeparation);
            graphviz.setNodeSeparation(nodeSeparation);
            graphviz.setMargin(margin);

            for (CustomView v : workspace.getViews().getCustomViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (SystemLandscapeView v : workspace.getViews().getSystemLandscapeViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (SystemContextView v : workspace.getViews().getSystemContextViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (ContainerView v : workspace.getViews().getContainerViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (ComponentView v : workspace.getViews().getComponentViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (DynamicView v : workspace.getViews().getDynamicViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            for (DeploymentView v : workspace.getViews().getDeploymentViews()) {
                if ((StringUtils.isNullOrEmpty(view) && v.getAutomaticLayout() != null) || v.getKey().equals(view)) {
                    graphviz.apply(v);
                }
            }

            try {
                File[] files = path.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                path.delete();
            } catch (Exception e) {
                // ignore
            }

            return WorkspaceUtils.toJson(workspace, false);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private RankDirection findRankDirection(String code) {
        for (RankDirection rankDirection : RankDirection.values()) {
            if (rankDirection.getCode().equals(code)) {
                return rankDirection;
            }
        }

        return RankDirection.TopBottom;
    }

}