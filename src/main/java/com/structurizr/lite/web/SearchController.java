package com.structurizr.lite.web;

import com.structurizr.lite.component.search.SearchComponent;
import com.structurizr.lite.component.search.SearchResult;
import com.structurizr.lite.domain.WorkspaceMetaData;
import com.structurizr.lite.util.HtmlUtils;
import com.structurizr.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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

        if (!StringUtils.isNullOrEmpty(query)) {
            List<SearchResult> searchResults = searchComponent.search(query, category);
            for (SearchResult searchResult : searchResults) {
                searchResult.setWorkspace(new WorkspaceMetaData(searchResult.getWorkspaceId()));
                filteredSearchResults.add(searchResult);
            }
        }

        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("results", filteredSearchResults);
        addCommonAttributes(model, "Search", true);

        return "search-results";
    }

}