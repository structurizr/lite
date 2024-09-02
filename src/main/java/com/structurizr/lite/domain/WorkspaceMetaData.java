package com.structurizr.lite.domain;

import java.util.Date;

public class WorkspaceMetaData {

    private final long workspaceId;
    private String name;
    private String description;
    private String version;
    private String thumbnail;
    private String apiKey;
    private String apiSecret;
    private Date lastModifiedDate;
    private String lastModifiedUser;
    private String lastModifiedAgent;

    private long size;
    private long revision = 0;

    private boolean editable = false;

    private String owner;

    public WorkspaceMetaData(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public long getId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public boolean isOpen() {
        return true;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getLastModifiedAgent() {
        return lastModifiedAgent;
    }

    public void setLastModifiedAgent(String lastModifiedAgent) {
        this.lastModifiedAgent = lastModifiedAgent;
    }

    public boolean isLocked() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public UserType getOwnerUserType() {
        return new UserType();
    }

    public boolean isClientEncrypted() {
        return false;
    }

    public boolean isLocal() {
        return false;
    }

    public boolean isCloud() {
        return !isLocal();
    }

    public String getApi() {
        return null;
    }

    public boolean isShareable() {
        return false;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getBranch() {
        return "";
    }

    public String getInternalVersion() {
        return "";
    }

    public boolean isActive() {
        return true;
    }

}