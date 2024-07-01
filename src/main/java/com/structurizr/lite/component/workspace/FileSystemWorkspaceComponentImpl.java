package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
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
import java.util.*;

@Component
class FileSystemWorkspaceComponentImpl implements WorkspaceComponent {

    private static final Log log = LogFactory.getLog(FileSystemWorkspaceComponentImpl.class);

    private File dataDirectory;
    private String filename;

    private String error;

    private long lastModifiedDate = 0;

    private final SearchComponent searchComponent;

    private final Map<Long, WorkspaceMetaData> workspaces = new TreeMap<>();

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

        if (Configuration.getInstance().isSingleWorkspace()) {
            File dsl = new File(getDataDirectory(1), filename + ".dsl");
            File json = new File(getDataDirectory(1), filename + ".json");

            if (!dsl.exists() && !json.exists()) {
                writeToFile(new File(dataDirectory, filename + ".dsl"), DSL_TEMPLATE);
            }

            workspaces.put(1L, toWorkspaceMetadata(loadWorkspace(1)));
        } else {
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file != null && file.isDirectory() && file.getName().matches("\\d*")) {
                        long id = Long.parseLong(file.getName());
                        workspaces.put(id, toWorkspaceMetadata(loadWorkspace(id)));
                    }
                }
            }
        }

        lastModifiedDate = findLatestLastModifiedDate(dataDirectory);
    }

    private WorkspaceMetaData toWorkspaceMetadata(Workspace workspace) {
        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(workspace.getId());
        workspaceMetaData.setName(workspace.getName());
        workspaceMetaData.setDescription(workspace.getDescription());
        workspaceMetaData.setLastModifiedDate(workspace.getLastModifiedDate());

        return workspaceMetaData;
    }

    private File getDataDirectory(long workspaceId) {
        if (Configuration.getInstance().isSingleWorkspace()) {
            return dataDirectory;
        } else {
            return new File(dataDirectory, "" + workspaceId);
        }
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

    private Workspace loadWorkspace(long workspaceId) {
        File dslFile = new File(getDataDirectory(workspaceId), filename + ".dsl");
        File jsonFile = new File(getDataDirectory(workspaceId), filename + ".json");

        Workspace workspace = null;
        if (dslFile.exists()) {
            workspace = loadWorkspaceFromDsl(workspaceId, dslFile, jsonFile);
        } else if (jsonFile.exists()) {
            workspace = loadWorkspaceFromJson(workspaceId, jsonFile);
        }

        if (workspace == null) {
            throw new NoWorkspaceFoundException(filename, getDataDirectory(workspaceId));
        }

        return workspace;
    }

    private Workspace loadWorkspaceFromJson(long workspaceId, File jsonFile) {
        Workspace workspace = null;

        if (jsonFile.exists()) {
            try {
                workspace = WorkspaceUtils.loadWorkspaceFromJson(jsonFile);
                workspace.setId(workspaceId);
                error = null;
            } catch (StructurizrDslParserException e) {
                throw new WorkspaceParsingException(e.getMessage());
            } catch (Exception e) {
                workspace = null;
                error = filename + ".json: " + e.getMessage();
                log.error(e);
            }
        }

        return workspace;
    }

    private Workspace loadWorkspaceFromDsl(long workspaceId, File dslFile, File jsonFile) {
        Workspace workspace = null;

        try {
            StructurizrDslParser parser = new StructurizrDslParser();
            parser.parse(dslFile);
            workspace = parser.getWorkspace();
            workspace.setId(workspaceId);

            // validate workspace scope
            WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

            if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
                workspace.getViews().createDefaultViews();
            }

            Workspace workspaceFromJson = loadWorkspaceFromJson(workspaceId, jsonFile);
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
        } catch (StructurizrDslParserException e) {
            throw new WorkspaceParsingException(e.getMessage());
        } catch (Exception e) {
            workspace = null;
            error = filename + ".dsl: " + e.getMessage();
            log.error(e);
        }

        return workspace;
    }

    public List<WorkspaceMetaData> getWorkspaces() {
        return new ArrayList<>(workspaces.values());
    }

    public Workspace getWorkspace(long workspaceId) {
        Workspace workspace = loadWorkspace(workspaceId);
        workspace.setId(workspaceId);

        return workspace;
    }

    @Override
    public void putWorkspace(Workspace workspace) throws WorkspaceComponentException {
        try {
            File jsonFile = new File(getDataDirectory(workspace.getId()), filename + ".json");
            workspace.setLastModifiedDate(DateUtils.removeMilliseconds(DateUtils.getNow()));
            WorkspaceUtils.saveWorkspaceToJson(workspace, jsonFile);

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
    public InputStreamAndContentLength getImage(long workspaceId, String diagramKey) throws WorkspaceComponentException {
        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId);
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
    public boolean putImage(long workspaceId, String filename, String imageAsBase64DataUri) {
        String base64Image = imageAsBase64DataUri.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));

        try {
            File imagesDirectory = getPathToWorkspaceImages(workspaceId);
            File file = new File(imagesDirectory, filename);
            Files.write(file.toPath(), decodedImage);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private File getPathToWorkspaceImages(long workspaceId) {
        File path = new File(new File(Configuration.getInstance().getWorkDirectory(), "" + workspaceId), "images");
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

        String dslFilename = filename + ".dsl";
        String jsonFilename = filename + ".json";

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(".") || file.getName().equals("structurizr.properties")) {
                    // ignore
                } else if (file.isFile()) {
                    if (file.getName().equals(jsonFilename) && new File(file.getParentFile(), dslFilename).exists()) {
                        // ignore JSON file updates if the DSL is being used as the authoring method
                        // e.g. ignore workspace.json if workspace.dsl exists in the same directory
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