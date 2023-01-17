package com.structurizr.lite.util;

import java.io.InputStream;

public final class InputStreamAndContentLength {

    private InputStream inputStream;
    private long contentLength;

    public InputStreamAndContentLength(InputStream inputStream, long contentLength) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getContentLength() {
        return contentLength;
    }

}