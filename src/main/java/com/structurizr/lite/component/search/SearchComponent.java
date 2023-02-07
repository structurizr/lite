package com.structurizr.lite.component.search;

import com.structurizr.Workspace;

import java.util.List;
import java.util.Set;

public interface SearchComponent {

    void start();

    void stop();

    void index(Workspace workspace);

    List<SearchResult> search(String query, String type);

}