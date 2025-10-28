package com.structurizr.lite.component.workspace;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.inspection.DefaultInspector;
import com.structurizr.lite.Configuration;
import com.structurizr.lite.component.search.SearchComponent;
import com.structurizr.lite.domain.WorkspaceMetaData;
import com.structurizr.lite.util.DateUtils;
import com.structurizr.lite.util.Image;
import com.structurizr.util.DslTemplate;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.structurizr.lite.component.workspace.WorkspaceDirectory.parseWorkspaceId;

@Component
class FileSystemWorkspaceComponentImpl implements WorkspaceComponent {

    private static final Log log = LogFactory.getLog(FileSystemWorkspaceComponentImpl.class);

    private static final String IMAGES_DIRECTORY = "images";

    private File dataDirectory;
    private String filename;

    private String error;

    private long lastModifiedDate = 0;

    private final SearchComponent searchComponent;

    FileSystemWorkspaceComponentImpl(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
        try {
            start();
        } catch (Exception e) {
            log.error("Error while starting Structurizr Lite", e);
        }
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
                writeToFile(new File(dataDirectory, filename + ".dsl"), DslTemplate.generate("Name", "Description"));
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
            File directory = new File(dataDirectory, "" + workspaceId);
            if (!directory.exists()) {
                File[] files = dataDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory() && parseWorkspaceId(file.getName()) == workspaceId) {
                            return file;
                        }
                    }
                }
            }

            return directory;
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

    private Workspace loadWorkspace(long workspaceId, boolean preferJson) {
        File workspaceDirectory = getDataDirectory(workspaceId);
        File dslFile = new File(workspaceDirectory, filename + ".dsl");
        File jsonFile = new File(workspaceDirectory, filename + ".json");

        if (preferJson) {
            if (jsonFile.exists()) {
                return loadWorkspaceFromJson(workspaceId, jsonFile);
            } else {
                return loadWorkspace(workspaceId, false);
            }
        } else {
            if (dslFile.exists()) {
                return loadWorkspaceFromDsl(workspaceId, dslFile, jsonFile);
            } else if (jsonFile.exists()) {
                Workspace workspace = loadWorkspaceFromJson(workspaceId, jsonFile);

                // if the JSON file exists and contains DSL, extract this and save it
                String embeddedDsl = DslUtils.getDsl(workspace);
                if (!StringUtils.isNullOrEmpty(embeddedDsl)) {
                    writeToFile(dslFile, embeddedDsl);
                }

                return workspace;
            } else {
                throw new NoWorkspaceFoundException(workspaceDirectory, filename);
            }
        }
    }

    private Workspace loadWorkspaceFromJson(long workspaceId, File jsonFile) {
        Workspace workspace = null;

        if (jsonFile.exists()) {
            try {
                workspace = WorkspaceUtils.loadWorkspaceFromJson(jsonFile);
                workspace.setId(workspaceId);
                error = null;
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
            parser.getHttpClient().allow(".*");
            parser.parse(dslFile);
            workspace = parser.getWorkspace();
            workspace.setId(workspaceId);

            // validate workspace scope
            WorkspaceScopeValidatorFactory.getValidator(workspace).validate(workspace);

            // run default inspections
            new DefaultInspector(workspace);
            
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
        } catch (Exception e) {
            workspace = null;
            error = filename + ".dsl: " + e.getMessage();
            log.error(e);
        }

        return workspace;
    }

    public List<WorkspaceMetaData> getWorkspaces() {
        List<WorkspaceMetaData> workspaces = new ArrayList<>();

        File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                long id = parseWorkspaceId(file.getName());
                if (file.isDirectory() && id > 0) {
                    try {
                        Workspace workspace = loadWorkspace(id, true);
                        if (workspace == null) {
                            workspace = new Workspace("Workspace " + id, "");
                            workspace.setId(id);
                        }
                        workspaces.add(toWorkspaceMetadata(workspace));
                    } catch (Exception e) {
                        log.warn("Ignoring workspace with ID " + id + ": " + e.getMessage());
                    }
                }
            }
        }

        return workspaces;
    }

    public Workspace getWorkspace(long workspaceId, boolean preferJson) {
        Workspace workspace = loadWorkspace(workspaceId, preferJson);

        if (workspace != null) {
            workspace.setId(workspaceId);
        }

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
    public Image getImage(long workspaceId, String filename) throws WorkspaceComponentException {
        if (isImage(filename)) {
            try {
                // first try .structurizr/{workspaceId}/images
                File file = new File(getPathToWorkspaceWorkDirectoryImages(workspaceId), filename);
                if (file.exists()) {
                    return new Image(file);
                } else {
                    // otherwise try {workspaceId}/images
                    file = new File(getPathToWorkspaceImages(workspaceId), filename);
                    if (file.exists()) {
                        return new Image(file);
                    }
                }
            } catch (Exception e) {
                String message = "Could not get image \"" + filename + "\" for workspace";
                log.warn(e.getMessage() + " - " + message);
            }
        } else {
            throw new WorkspaceComponentException(filename + " is not an image");
        }

        return null;
    }

    @Override
    public boolean putImage(long workspaceId, String filename, String imageAsBase64DataUri) throws WorkspaceComponentException {
        String base64Image = imageAsBase64DataUri.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));

        if (isImage(filename)) {
            try {
                File imagesDirectory = getPathToWorkspaceWorkDirectoryImages(workspaceId);
                File file = new File(imagesDirectory, filename);
                Files.write(file.toPath(), decodedImage);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new WorkspaceComponentException(filename + " is not an image");
        }

        return false;
    }

    private boolean isImage(String filename) {
        if (StringUtils.isNullOrEmpty(filename)) {
            return false;
        }

        filename = filename.toLowerCase();
        return filename.endsWith(".jpg") || filename.endsWith(".jepg") || filename.endsWith(".png") || filename.endsWith(".gif");
    }

    private File getPathToWorkspaceWorkDirectoryImages(long workspaceId) {
        File path = new File(new File(Configuration.getInstance().getWorkDirectory(), "" + workspaceId), IMAGES_DIRECTORY);
        if (!path.exists()) {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }

        return path;
    }

    private File getPathToWorkspaceImages(long workspaceId) {
        return new File(getDataDirectory(workspaceId), IMAGES_DIRECTORY);
    }

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