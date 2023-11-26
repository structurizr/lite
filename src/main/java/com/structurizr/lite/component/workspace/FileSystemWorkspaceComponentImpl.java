package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.search.SearchComponent;
import com.structurizr.lite.util.DateUtils;
import com.structurizr.lite.util.InputStreamAndContentLength;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Component
class FileSystemWorkspaceComponentImpl implements WorkspaceComponent {

    private static Log log = LogFactory.getLog(FileSystemWorkspaceComponentImpl.class);

    private File dataDirectory;
    private String filename;

    private String error;

    private long lastModifiedDate = 0;

    private SearchComponent searchComponent;

    FileSystemWorkspaceComponentImpl(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
        start();
    }

    public void start() {
        this.dataDirectory = Configuration.getInstance().getDataDirectory();
        this.filename = Configuration.getInstance().getWorkspaceFilename();

        if (dataDirectory.exists() && dataDirectory.isFile()) {
            throw new RuntimeException(dataDirectory.getAbsolutePath() + " should be a directory, not a file - stopping Structurizr Lite.");
        }

        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        if (!Configuration.getInstance().getWorkDirectory().exists()) {
            Configuration.getInstance().getWorkDirectory().mkdirs();
        }

        File dsl = new File(dataDirectory, filename + ".dsl");
        File json = new File(dataDirectory, filename + ".json");

        if (!dsl.exists() && !json.exists()) {
            createTemplate();
        }

        lastModifiedDate = findLatestLastModifiedDate(dataDirectory);
    }

    public void createTemplate() {
        writeToFile(new File(dataDirectory, filename + ".dsl"), DSL_TEMPLATE);
    }

    private void writeToFile(File file, String content) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private Workspace loadWorkspace() {
        File dsl = new File(dataDirectory, filename + ".dsl");
        File json = new File(dataDirectory, filename + ".json");

        if (dsl.exists()) {
            return loadWorkspaceFromDsl();
        } else if (json.exists()) {
            return loadWorkspaceFromJson();
        } else {
            throw new NoWorkspaceFoundException(filename);
        }
    }

    private Workspace loadWorkspaceFromJson() {
        Workspace workspace = null;

        try {
            File file = new File(dataDirectory, filename + ".json");
            if (file.exists()) {
                workspace = WorkspaceUtils.loadWorkspaceFromJson(file);
                workspace.setId(1);
                error = null;
            }
        } catch (Exception e) {
            workspace = null;
            error = filename + ".json: " + e.getMessage();
            log.error(e);
        }

        return workspace;
    }

    private Workspace loadWorkspaceFromDsl() {
        Workspace workspace = null;

        try {
            File file = new File(dataDirectory, filename + ".dsl");
            if (file.exists()) {
                StructurizrDslParser parser = new StructurizrDslParser();
                parser.parse(file);
                workspace = parser.getWorkspace();
                workspace.setId(1);

                // validate workspace scope
                WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

                if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
                    workspace.getViews().createDefaultViews();
                }

                Workspace workspaceFromJson = loadWorkspaceFromJson();
                if (workspaceFromJson != null) {
                    workspace.getViews().copyLayoutInformationFrom(workspaceFromJson.getViews());
                    workspace.getViews().getConfiguration().copyConfigurationFrom(workspaceFromJson.getViews().getConfiguration());
                }

                workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));

                try {
                    putWorkspace(workspace);
                } catch (Exception e) {
                    log.warn(e);
                }

                error = null;
            }
        } catch (Exception e) {
            workspace = null;
            error = filename + ".dsl: " + e.getMessage();
            log.error(e);
        }

        return workspace;
    }

    public Workspace getWorkspace() {
        return loadWorkspace();
    }

    @Override
    public void putWorkspace(Workspace workspace) throws WorkspaceComponentException {
        try {
            File file = new File(dataDirectory, filename + ".json");
            workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));
            WorkspaceUtils.saveWorkspaceToJson(workspace, file);

            try {
                searchComponent.index(workspace);
            } catch (Exception e) {
                log.warn(e);
            }
        } catch (Exception e) {
            log.error(e);
            throw new WorkspaceComponentException(e.getMessage());
        }
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public InputStreamAndContentLength getImage(String diagramKey) throws WorkspaceComponentException {
        try {
            File imagesDirectory = getPathToWorkspaceImages();
            File file = new File(imagesDirectory, diagramKey);
            if (file.exists()) {
                return new InputStreamAndContentLength(new FileInputStream(file), file.length());
            }
        } catch (Exception e) {
            String message = "Could not get " + diagramKey + " for workspace";
            log.warn(e.getMessage() + " - " + message);
        }

        return null;
    }

    @Override
    public boolean putImage(String filename, String imageAsBase64DataUri) {
        String base64Image = imageAsBase64DataUri.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));

        try {
            File imagesDirectory = getPathToWorkspaceImages();
            File file = new File(imagesDirectory, filename);
            Files.write(file.toPath(), decodedImage);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private File getPathToWorkspaceImages() {
        File path = new File(Configuration.getInstance().getWorkDirectory(), "images");
        if (!path.exists()) {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    private static final String DSL_TEMPLATE = """
            workspace {
                        
                model {
                    user = person "User"
                    softwareSystem = softwareSystem "Software System"
                        
                    user -> softwareSystem "Uses"
                }
                        
                views {
                    systemContext softwareSystem "Diagram1" {
                        include *
                    }
                }
                        
                configuration {
                    scope softwaresystem
                }
                        
            }""";

    @Scheduled(fixedDelayString = "#{@applicationPropertyService.getAutoRefreshInterval()}")
    public void checkForUpdatedFiles() {
        lastModifiedDate = findLatestLastModifiedDate(dataDirectory);
    }

    private long findLatestLastModifiedDate(File directory) {
        long timestamp = 0;

        File dsl = new File(dataDirectory, filename + ".dsl");

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(".") || file.getName().equals("structurizr.properties")) {
                    // ignore
                } else if (file.isFile()) {
                    if (file.getName().endsWith(".json") && dsl.exists()) {
                        // ignore JSON file updates if the DSL is being used as the authoring method
                    } else {
                        timestamp = Math.max(timestamp, file.lastModified());
                    }
                } else if (file.isDirectory()) {
                    timestamp = Math.max(timestamp, findLatestLastModifiedDate(file));
                }
            }
        }

        return timestamp;
    }

    @Override
    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

}

@Service
class ApplicationPropertyService {

    public String getAutoRefreshInterval() {
        int interval = Configuration.getInstance().getAutoRefreshInterval();
        if (interval == 0) {
            interval = Integer.MAX_VALUE;
        }

        return "" + interval;
    }
}