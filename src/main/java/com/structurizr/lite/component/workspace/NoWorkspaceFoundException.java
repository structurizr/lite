package com.structurizr.lite.component.workspace;

import java.io.File;

public class NoWorkspaceFoundException extends RuntimeException {

    NoWorkspaceFoundException(File directory, String filename) {
        super(
                directory.exists()
                ?
                String.format("No %s.dsl or %s.json file was found in %s.", filename, filename, directory.getAbsolutePath())
                :
                String.format("The workspace directory %s does not exist.", directory.getAbsolutePath())
        );
    }

}