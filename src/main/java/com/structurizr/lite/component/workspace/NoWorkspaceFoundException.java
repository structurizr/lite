package com.structurizr.lite.component.workspace;

import java.io.File;

public class NoWorkspaceFoundException extends RuntimeException {

    NoWorkspaceFoundException(String filename, File directory) {
        super(String.format("No %s.dsl or %s.json file was found in %s.", filename, filename, directory.getAbsolutePath()));
    }

}