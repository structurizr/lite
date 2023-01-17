package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.lite.util.InputStreamAndContentLength;

public interface WorkspaceComponent {

    void start() throws Exception;

    void createTemplate();

    Workspace getWorkspace();

    void putWorkspace(Workspace workspace) throws WorkspaceComponentException;

    String getError();

    InputStreamAndContentLength getImage(String diagramKey) throws WorkspaceComponentException;

    boolean putImage(String filename, String imageAsBase64) throws WorkspaceComponentException;

    long getLastModifiedDate();

}