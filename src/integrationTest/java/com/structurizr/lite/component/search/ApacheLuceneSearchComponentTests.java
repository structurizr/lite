package com.structurizr.lite.component.search;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Format;
import com.structurizr.documentation.Section;
import com.structurizr.lite.Configuration;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApacheLuceneSearchComponentTests {

    private static final File DATA_DIRECTORY = new File("./build/ApacheLuceneSearchComponentTests");

    private File dataDirectory;
    private ApacheLuceneSearchComponentImpl searchComponent;

    @BeforeEach
    public void setUp() throws Exception {
        DATA_DIRECTORY.mkdirs();
        dataDirectory = Files.createTempDirectory(DATA_DIRECTORY.toPath(), "").toFile();
        Configuration.init(dataDirectory);
        searchComponent = new ApacheLuceneSearchComponentImpl(dataDirectory);
    }

    @AfterEach
    public void tearDown() {
        FileSystemUtils.deleteRecursively(dataDirectory);
    }

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("name", null);
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals("workspace", result.getType());
        assertEquals("/workspace", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("Description", result.getDescription());
    }

    @Test
    public void index_ReplacesTheWorkspaceInTheSearchIndexWhenItAlreadyExists() throws Exception {
        Workspace workspace = new Workspace("Name", "Old");
        workspace.setId(1);
        searchComponent.index(workspace);

        workspace.setDescription("New");
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("new", null);
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(1, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("/workspace", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("New", result.getDescription());
    }

    @Test
    public void search_FiltersResultsByType() throws Exception {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("name", null);
        assertEquals(1, results.size());

        results = searchComponent.search("name", DocumentType.WORKSPACE);
        assertEquals(1, results.size());

        results = searchComponent.search("name", DocumentType.DOCUMENTATION);
        assertEquals(0, results.size());
    }

    @Test
    public void search_WorkspaceDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        workspace.getDocumentation().addSection(new Section(Format.Markdown, content));

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("W - Section 1", results.get(0).getName());
        assertEquals("/workspace/documentation#1", results.get(0).getUrl());

        results = searchComponent.search("bar", null);
        assertEquals(1, results.size());
        assertEquals("W - Section 2", results.get(0).getName());
        assertEquals("/workspace/documentation#2", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        softwareSystem.getDocumentation().addSection(new Section(Format.Markdown, content));

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("A - Section 1", results.get(0).getName());
        assertEquals("/workspace/documentation/A#1", results.get(0).getUrl());

        results = searchComponent.search("bar", null);
        assertEquals(1, results.size());
        assertEquals("A - Section 2", results.get(0).getName());
        assertEquals("/workspace/documentation/A#2", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        container.getDocumentation().addSection(new Section(Format.Markdown, content));

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("B - Section 1", results.get(0).getName());
        assertEquals("/workspace/documentation/A/B#1", results.get(0).getUrl());

        results = searchComponent.search("bar", null);
        assertEquals(1, results.size());
        assertEquals("B - Section 2", results.get(0).getName());
        assertEquals("/workspace/documentation/A/B#2", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDocumentation() throws Exception {
        String content =
                """
## Section 1

Foo

## Section 2

Bar
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Component component = container.addComponent("C");
        component.getDocumentation().addSection(new Section(Format.Markdown, content));

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("C - Section 1", results.get(0).getName());
        assertEquals("/workspace/documentation/A/B/C#1", results.get(0).getUrl());

        results = searchComponent.search("bar", null);
        assertEquals(1, results.size());
        assertEquals("C - Section 2", results.get(0).getName());
        assertEquals("/workspace/documentation/A/B/C#2", results.get(0).getUrl());
    }

    @Test
    public void search_SoftwareSystemDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        softwareSystem.getDocumentation().addDecision(decision);

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("A - 1. Title", results.get(0).getName());
        assertEquals("/workspace/decisions/A#1", results.get(0).getUrl());
    }

    @Test
    public void search_ContainerDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        container.getDocumentation().addDecision(decision);

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("B - 1. Title", results.get(0).getName());
        assertEquals("/workspace/decisions/A/B#1", results.get(0).getUrl());
    }

    @Test
    public void search_ComponentDecisions() throws Exception {
        String content =
                """
## Context

Foo
                """;

        Workspace workspace = new Workspace("W", "Description");
        workspace.setId(1);
        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("A");
        Container container = softwareSystem.addContainer("B");
        Component component = container.addComponent("C");
        Decision decision = new Decision("1");
        decision.setTitle("Title");
        decision.setStatus("Accepted");
        decision.setFormat(Format.Markdown);
        decision.setContent(content);
        component.getDocumentation().addDecision(decision);

        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("foo", null);
        assertEquals(1, results.size());
        assertEquals("C - 1. Title", results.get(0).getName());
        assertEquals("/workspace/decisions/A/B/C#1", results.get(0).getUrl());
    }

}