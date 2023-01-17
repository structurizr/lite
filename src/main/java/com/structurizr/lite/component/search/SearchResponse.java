package com.structurizr.lite.component.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

class SearchResponse {

    private SearchHits hits;

    SearchResponse() {
    }

    public SearchHits getHits() {
        return hits;
    }

    public void setHits(SearchHits hits) {
        this.hits = hits;
    }

}

class SearchHits {

    private List<SearchHit> hits;

    public SearchHits() {
    }

    public List<SearchHit> getHits() {
        return hits;
    }

    public void setHits(List<SearchHit> hits) {
        this.hits = hits;
    }

}

class SearchHit {

    @JsonProperty("_source")
    private Document source;

    SearchHit() {
    }

    public Document getSource() {
        return source;
    }

    public void setSource(Document source) {
        this.source = source;
    }

}
