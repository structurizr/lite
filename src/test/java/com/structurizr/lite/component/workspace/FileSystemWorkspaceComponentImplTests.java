package com.structurizr.lite.component.workspace;

import com.structurizr.lite.Configuration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemWorkspaceComponentImplTests {

    @Test
    void getImage_ThrowsException_WhenRequestingAFileThatIsNotAnImage() throws Exception {
        Path tmpdir = Files.createTempDirectory(Paths.get("build"), getClass().getSimpleName());
        Configuration.init(tmpdir.toFile());
        WorkspaceComponent workspaceComponent = new FileSystemWorkspaceComponentImpl(null);

        try {
            workspaceComponent.getImage(1, "xss.js");
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("xss.js is not an image", e.getMessage());
        }
    }

    @Test
    void putImage_ThrowsException_WhenPuttingAFileThatIsNotAnImage() throws Exception {
        Path tmpdir = Files.createTempDirectory(Paths.get("build"), getClass().getSimpleName());
        Configuration.init(tmpdir.toFile());
        WorkspaceComponent workspaceComponent = new FileSystemWorkspaceComponentImpl(null);

        try {
            workspaceComponent.putImage(1, "xss.js", "data:text/javascript;base64,YWxlcnQoJ1hTUycpOw==");
            fail();
        } catch (WorkspaceComponentException e) {
            assertEquals("xss.js is not an image", e.getMessage());
        }
    }

}