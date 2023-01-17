package com.structurizr.lite.web;

import com.structurizr.lite.component.search.SearchComponent;
import com.structurizr.lite.component.search.SearchResult;
import com.structurizr.lite.component.workspace.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController extends AbstractController {

    @Autowired
    private SearchComponent searchComponent;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showDashboardPage(ModelMap model,
                                    @RequestParam(required = false) String query,
                                    @RequestParam(required = false) String category) {

        List<SearchResult> filteredSearchResults = new ArrayList<>();

        if (query != null) {
            query = HtmlUtils.filterHtml(query);
            query = query.replaceAll("\"", "");
        }

        if (category != null) {
            category = HtmlUtils.filterHtml(category);
            category = category.replaceAll("\"", "");
            category = category.toLowerCase();
        }

        WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData();
        Map<Long,WorkspaceMetaData> workspacesById = new HashMap<>();
        workspacesById.put(1L, workspaceMetaData);

        if (!StringUtils.isNullOrEmpty(query)) {
            List<SearchResult> searchResults = searchComponent.search(query, category, workspacesById.keySet());
            for (SearchResult searchResult : searchResults) {
                if (workspacesById.containsKey(searchResult.getWorkspaceId())) {
                    searchResult.setWorkspace(workspacesById.get(searchResult.getWorkspaceId()));
                    filteredSearchResults.add(searchResult);
                }
            }
        }

        model.addAttribute("query", query);
        model.addAttribute("workspaceId", "1");
        model.addAttribute("category", category);
        model.addAttribute("results", filteredSearchResults);
        model.addAttribute("urlPrefix", "/workspace");
        addCommonAttributes(model, "Search", true);

        return "search-results";
    }

}