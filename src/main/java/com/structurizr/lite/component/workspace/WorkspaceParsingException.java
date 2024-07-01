package com.structurizr.lite.component.workspace;

public class WorkspaceParsingException extends RuntimeException {

    public WorkspaceParsingException(String message) {
        super(message);
    }

    public WorkspaceParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}