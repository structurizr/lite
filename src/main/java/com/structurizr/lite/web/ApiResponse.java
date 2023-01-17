package com.structurizr.lite.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiResponse {

    private boolean success = false;
    private String message = "";
    private Long revision = null;

    public ApiResponse() {
    }

    public ApiResponse(String message) {
        this(true, message);
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    ApiResponse(Exception e) {
        this(false, e.getMessage());
    }

    public boolean isSuccess() {
        return success;
    }

    void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long getRevision() {
        return revision;
    }

    void setRevision(Long revision) {
        this.revision = revision;
    }

    static ApiResponse parse(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        return objectMapper.readValue(json, ApiResponse.class);
    }

}