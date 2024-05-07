package com.structurizr.lite.component.search;

import com.structurizr.Workspace;

import java.util.List;

public interface SearchComponent {

    void index(Workspace workspace);

    List<SearchResult> search(String query, String type);

}