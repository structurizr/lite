package com.structurizr.lite.web;

import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.util.Locale;

public class RawViewResolver extends UrlBasedViewResolver {

    @Override
    protected boolean canHandle(String viewName, Locale locale) {
        return "json".equals(viewName);
    }

}