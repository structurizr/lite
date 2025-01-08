package com.structurizr.lite;

import com.structurizr.lite.util.RandomGuidGenerator;
import com.structurizr.lite.util.Version;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Configuration {

    private static final String STRUCTURIZR_WORKSPACE_PATH = "STRUCTURIZR_WORKSPACE_PATH";
    private static final String STRUCTURIZR_WORKSPACE_FILENAME = "STRUCTURIZR_WORKSPACE_FILENAME";
    private static final String DEFAULT_WORKSPACE_FILENAME = "workspace";
    private static final String STRUCTURIZR_PROPERTIES_FILENAME = "structurizr.properties";

    private static final String STRUCTURIZR_CLOUD_SERVICE_API_URL = "https://api.structurizr.com";
    private static final String WORK_DIRECTORY_NAME = ".structurizr";

    private static final String WORKSPACES_PROPERTY = "structurizr.workspaces";
    private static final String EDITABLE_PROPERTY = "structurizr.editable";
    private static final String URL_PROPERTY = "structurizr.url";
    private static final String AUTO_SAVE_INTERVAL_PROPERTY = "structurizr.autoSaveInterval";
    private static final String DEFAULT_AUTO_SAVE_INTERVAL_IN_MILLISECONDS = "5000";
    private static final String AUTO_REFRESH_INTERVAL_PROPERTY = "structurizr.autoRefreshInterval";
    private static final String DEFAULT_AUTO_REFRESH_INTERVAL_IN_MILLISECONDS = "0";

    private static final String REMOTE_WORKSPACE_API_URL_PROPERTY = "structurizr.remote.apiUrl";
    private static final String REMOTE_WORKSPACE_API_KEY_PROPERTY = "structurizr.remote.apiKey";
    private static final String REMOTE_WORKSPACE_API_SECRET_PROPERTY = "structurizr.remote.apiSecret";
    private static final String REMOTE_WORKSPACE_PASSPHRASE_PROPERTY = "structurizr.remote.passphrase";
    private static final String REMOTE_WORKSPACE_ID_PROPERTY = "structurizr.remote.workspaceId";
    private static final String REMOTE_WORKSPACE_BRANCH_PROPERTY = "structurizr.remote.branch";
    private static final String SINGLE_WORKSPACE = "1";

    public static final boolean PREVIEW_FEATURES = false;

    private File dataDirectory;
    private String webUrl;
    private final String versionSuffix;

    private final String apiKey = new RandomGuidGenerator().generate();
    private final String apiSecret = new RandomGuidGenerator().generate();

    private boolean graphvizEnabled = false;

    private final static Configuration INSTANCE = new Configuration();

    static {
        initLogger();
    }

    private Configuration() {
        String buildNumber = new Version().getBuildNumber();
        if (StringUtils.isNullOrEmpty(buildNumber)) {
            versionSuffix = "";
        } else {
            versionSuffix = "-" + buildNumber;
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String url) {
        if (url != null) {
            if (url.endsWith("/")) {
                this.webUrl = url.substring(0, url.length()-1);
            } else {
                this.webUrl = url;
            }
        }
    }

    public String getCdnUrl() {
        return webUrl + "/static";
    }

    public String getVersionSuffix() {
        return versionSuffix;
    }

    public String getApiUrl() {
        return webUrl + "/api";
    }

    public String getGraphvizUrl() {
        return webUrl + "/graphviz";
    }

    public boolean isCloud() {
        return false;
    }

    public String getType() {
        return "lite";
    }

    public String getProduct() {
        return "lite";
    }

    public void setGraphvizEnabled(boolean b) {
        this.graphvizEnabled = b;
    }

    public boolean isGraphvizEnabled() {
        return graphvizEnabled;
    }

    public boolean isSafeMode() {
        return false;
    }

    public File getDataDirectory() {
        String directory = getEnvironmentVariable(STRUCTURIZR_WORKSPACE_PATH);
        if (directory != null) {
            return new File(dataDirectory, directory);
        } else {
            return dataDirectory;
        }
    }

    public String getWorkspaceFilename() {
        return getEnvironmentVariable(STRUCTURIZR_WORKSPACE_FILENAME, DEFAULT_WORKSPACE_FILENAME);
    }

    public boolean isSingleWorkspace() {
        return getConfigurationParameter(WORKSPACES_PROPERTY, SINGLE_WORKSPACE).equals(SINGLE_WORKSPACE);
    }

    public File getWorkDirectory() {
        return new File(dataDirectory, WORK_DIRECTORY_NAME);
    }

    void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;

        setWebUrl(getConfigurationParameter(URL_PROPERTY, ""));
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public long getRemoteWorkspaceId() {
        return Long.parseLong(getConfigurationParameter(REMOTE_WORKSPACE_ID_PROPERTY, "0"));
    }

    public String getRemoteBranch() {
        return getConfigurationParameter(REMOTE_WORKSPACE_BRANCH_PROPERTY, "");
    }

    public String getRemoteApiUrl() {
        return getConfigurationParameter(REMOTE_WORKSPACE_API_URL_PROPERTY, STRUCTURIZR_CLOUD_SERVICE_API_URL);
    }

    public String getRemoteApiKey() {
        return getConfigurationParameter(REMOTE_WORKSPACE_API_KEY_PROPERTY, "");
    }

    public String getRemoteApiSecret() {
        return getConfigurationParameter(REMOTE_WORKSPACE_API_SECRET_PROPERTY, "");
    }

    public String getRemotePassphrase() {
        return getConfigurationParameter(REMOTE_WORKSPACE_PASSPHRASE_PROPERTY, "");
    }

    public int getAutoSaveInterval() {
        return Integer.parseInt(getConfigurationParameter(AUTO_SAVE_INTERVAL_PROPERTY, DEFAULT_AUTO_SAVE_INTERVAL_IN_MILLISECONDS));
    }

    public int getAutoRefreshInterval() {
        return Integer.parseInt(getConfigurationParameter(AUTO_REFRESH_INTERVAL_PROPERTY, DEFAULT_AUTO_REFRESH_INTERVAL_IN_MILLISECONDS));
    }

    public boolean isEditable() {
        return Boolean.parseBoolean(getConfigurationParameter(EDITABLE_PROPERTY, "true"));
    }

    private String getEnvironmentVariable(String name) {
        return getEnvironmentVariable(name, null);
    }

    private String getEnvironmentVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        if (StringUtils.isNullOrEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public String getConfigurationParameter(String structurizrPropertyName, String defaultValue) {
        String value = null;

        File file = new File(getDataDirectory(), STRUCTURIZR_PROPERTIES_FILENAME);
        if (file.exists()) {
            try {
                Properties properties = new Properties();
                FileReader fileReader = new FileReader(file);
                properties.load(fileReader);

                if (properties.containsKey(structurizrPropertyName)) {
                    value = properties.getProperty(structurizrPropertyName);
                }
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (value == null) {
            value = defaultValue;
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    private static void initLogger() {
        class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

            private Log log = LogFactory.getLog(UncaughtExceptionHandler.class);

            public void uncaughtException(Thread t, Throwable ex) {
                log.error("Uncaught exception in thread: " + t.getName(), ex);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

}