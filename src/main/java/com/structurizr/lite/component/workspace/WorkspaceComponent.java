package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.lite.util.InputStreamAndContentLength;

public interface WorkspaceComponent {

    void start() throws Exception;

    Workspace getWorkspace(long workspaceId);

    void putWorkspace(Workspace workspace) throws WorkspaceComponentException;

    String getError();

    InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException;

    boolean putImage(long workspaceId, String filename, String imageAsBase64) throws WorkspaceComponentException;

    long getLastModifiedDate();

}