package com.structurizr.lite.component.workspace;

public class NoWorkspaceFoundException extends RuntimeException {

    NoWorkspaceFoundException(String filename) {
        super(String.format("No %s.dsl or %s.json file was found.", filename, filename));
    }

}