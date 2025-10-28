package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.lite.domain.WorkspaceMetaData;
import com.structurizr.lite.util.Image;

import java.util.List;

/**
 * Provides access to workspace data stored on the file system.
 */
public interface WorkspaceComponent {

    void start() throws Exception;

    List<WorkspaceMetaData> getWorkspaces();

    Workspace getWorkspace(long workspaceId, boolean preferJson);

    void putWorkspace(Workspace workspace) throws WorkspaceComponentException;

    String getError();

    Image getImage(long workspaceId, String filename) throws WorkspaceComponentException;

    boolean putImage(long workspaceId, String filename, String imageAsBase64) throws WorkspaceComponentException;

    long getLastModifiedDate();

}