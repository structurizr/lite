package com.structurizr.lite.component.search;

import com.structurizr.lite.component.workspace.WorkspaceMetaData;

public final class SearchResult {

    private long workspaceId;
    private WorkspaceMetaData workspace;
    private String url;
    private String name;
    private String description;
    private String type;

    public SearchResult(long workspaceId, String url, String name, String description, String type) {
        this.workspaceId = workspaceId;
        if (url.startsWith("/" + workspaceId)) {
            this.url = url.substring(("/" + workspaceId).length());
        } else {
            this.url = url;
        }
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMetaData getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceMetaData workspace) {
        this.workspace = workspace;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

}