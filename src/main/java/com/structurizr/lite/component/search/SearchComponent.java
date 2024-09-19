package com.structurizr.lite.component.search;

import com.structurizr.Workspace;

import java.util.List;

/**
 * Provides workspace search facilities using Apache Lucene.
 */
public interface SearchComponent {

    void index(Workspace workspace);

    List<SearchResult> search(String query, String type);

}