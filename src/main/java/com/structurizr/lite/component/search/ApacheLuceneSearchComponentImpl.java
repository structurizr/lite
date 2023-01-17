package com.structurizr.lite.component.search;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentation;
import com.structurizr.documentation.Section;
import com.structurizr.lite.Configuration;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.ElementView;
import com.structurizr.view.RelationshipView;
import com.structurizr.view.View;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Component
class ApacheLuceneSearchComponentImpl implements SearchComponent {

    private static Log log = LogFactory.getLog(ApacheLuceneSearchComponentImpl.class);
    
    private static final String URL_KEY = "url";
    private static final String WORKSPACE_KEY = "workspace";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String TYPE_KEY = "type";
    private static final String CONTENT_KEY = "content";

    private File indexDirectory;

    ApacheLuceneSearchComponentImpl() {
        indexDirectory = new File(Configuration.getInstance().getWorkDirectory(), "index");
    }

    @Override
    public void start() {
        if (!indexDirectory.exists()) {
            try {
                Files.createDirectory(indexDirectory.toPath());
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void index(Workspace workspace) {
        try {
            FileSystemUtils.deleteRecursively(indexDirectory);
            Files.createDirectory(indexDirectory.toPath());

            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            Directory dir = FSDirectory.open(indexDirectory.toPath());
            IndexWriter writer = new IndexWriter(dir, iwc);

            delete(workspace.getId(), writer);

            Document doc = new Document();
            doc.add(new StoredField(URL_KEY, ""));
            doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
            doc.add(new TextField(TYPE_KEY, DocumentType.WORKSPACE, Field.Store.YES));
            doc.add(new StoredField(NAME_KEY, workspace.getName()));
            doc.add(new StoredField(DESCRIPTION_KEY, workspace.getDescription()));
            doc.add(new TextField(CONTENT_KEY, appendAll(workspace.getName(), workspace.getDescription()), Field.Store.NO));
            writer.addDocument(doc);

            for (View view : workspace.getViews().getCustomViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getSystemLandscapeViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getSystemContextViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getContainerViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getComponentViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getDynamicViews()) {
                index(workspace, view, writer);
            }
            for (View view : workspace.getViews().getDeploymentViews()) {
                index(workspace, view, writer);
            }

            index(workspace, null, workspace.getDocumentation(), writer);
            for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
                index(workspace, softwareSystem, softwareSystem.getDocumentation(), writer);
            }

            writer.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void delete(long workspaceId, IndexWriter indexWriter) {
        try {
            Term workspaceIdTerm = new Term(WORKSPACE_KEY, toString(workspaceId));
            indexWriter.deleteDocuments(workspaceIdTerm);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void index(Workspace workspace, View view, IndexWriter indexWriter) throws Exception {
        Document doc = new Document();
        doc.add(new StoredField(URL_KEY, "/diagrams#" + view.getKey()));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DIAGRAM, Field.Store.YES));
        doc.add(new StoredField(NAME_KEY, view.getName()));
        doc.add(new StoredField(DESCRIPTION_KEY, view.getDescription()));

        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(view.getTitle())) {
            content.append(view.getTitle());
            content.append(" ");
        } else if (!StringUtils.isNullOrEmpty(view.getName())) {
            content.append(view.getName());
            content.append(" ");
        }

        if (!StringUtils.isNullOrEmpty(view.getDescription())) {
            content.append(view.getDescription());
            content.append(" ");
        }

        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();

            if (element instanceof CustomElement || element instanceof Person || element instanceof SoftwareSystem) {
                content.append(indexElementBasics(element));
            }

            if (element instanceof Container) {
                content.append(indexElementBasics(element));
                String technology = ((Container)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof Component) {
                content.append(indexElementBasics(element));
                String technology = ((Component)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof DeploymentNode) {
                content.append(index((DeploymentNode)element));
                content.append(" ");
            }
        }

        for (RelationshipView relationshipView : view.getRelationships()) {
            Relationship relationship = relationshipView.getRelationship();
            if (!StringUtils.isNullOrEmpty(relationship.getDescription())) {
                content.append(relationship.getDescription());
                content.append(" ");
            }

            if (!StringUtils.isNullOrEmpty(relationship.getTechnology())) {
                content.append(relationship.getTechnology());
                content.append(" ");
            }
        }

        doc.add(new TextField(CONTENT_KEY, content.toString(), Field.Store.NO));

        indexWriter.addDocument(doc);
    }

    private String indexElementBasics(Element element) {
        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(element.getName())) {
            content.append(element.getName());
            content.append(" ");
        }
        if (!StringUtils.isNullOrEmpty(element.getDescription())) {
            content.append(element.getDescription());
            content.append(" ");
        }

        return content.toString();
    }

    private String index(DeploymentNode deploymentNode) {
        StringBuilder content = new StringBuilder();

        content.append(indexElementBasics(deploymentNode));
        String technology = deploymentNode.getTechnology();
        if (!StringUtils.isNullOrEmpty(technology)) {
            content.append(technology);
            content.append(" ");
        }

        for (DeploymentNode child : deploymentNode.getChildren()) {
            content.append(index(child));
        }

        for (InfrastructureNode infrastructureNode : deploymentNode.getInfrastructureNodes()) {
            content.append(indexElementBasics(infrastructureNode));
            String infrastructureNodeTechnology = infrastructureNode.getTechnology();
            if (!StringUtils.isNullOrEmpty(infrastructureNodeTechnology)) {
                content.append(infrastructureNodeTechnology);
                content.append(" ");
            }
        }

        for (SoftwareSystemInstance softwareSystemInstance : deploymentNode.getSoftwareSystemInstances()) {
            SoftwareSystem softwareSystem = softwareSystemInstance.getSoftwareSystem();
            content.append(indexElementBasics(softwareSystem));
        }

        for (ContainerInstance containerInstance : deploymentNode.getContainerInstances()) {
            Container container = containerInstance.getContainer();
            content.append(indexElementBasics(container));
            String containerInstanceTechnology = container.getTechnology();
            if (!StringUtils.isNullOrEmpty(containerInstanceTechnology)) {
                content.append(containerInstanceTechnology);
                content.append(" ");
            }
        }

        return content.toString();
    }

    private void index(Workspace workspace, SoftwareSystem softwareSystem, Documentation documentation, IndexWriter indexWriter) throws Exception {
        if (documentation != null) {
            for (Section section : documentation.getSections()) {
                index(workspace, softwareSystem, section, indexWriter);
            }

            for (Decision decision : documentation.getDecisions()) {
                index(workspace, softwareSystem, decision, indexWriter);
            }
        }
    }

    private void index(Workspace workspace, Element element, Section section, IndexWriter indexWriter) throws Exception {
        Document doc = new Document();

        // some backwards compatibility for older documentation
        if (element == null && !StringUtils.isNullOrEmpty(section.getElementId())) {
            element = workspace.getModel().getElement(section.getElementId());
        }

        doc.add(new StoredField(URL_KEY, "/documentation/" + calculateUrl(element, section)));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DOCUMENTATION, Field.Store.YES));
        if (element == null) {
            doc.add(new StoredField(NAME_KEY, section.getTitle()));
        } else {
            doc.add(new StoredField(NAME_KEY, element.getName() + " - " + section.getTitle()));
        }
        doc.add(new StoredField(DESCRIPTION_KEY, ""));
        doc.add(new TextField(CONTENT_KEY, appendAll(section.getTitle(), section.getContent()), Field.Store.NO));
        indexWriter.addDocument(doc);
    }

    private void index(Workspace workspace, Element element, Decision decision, IndexWriter indexWriter) throws Exception {
        Document doc = new Document();

        // some backwards compatibility for older documentation
        if (element == null && !StringUtils.isNullOrEmpty(decision.getElementId())) {
            element = workspace.getModel().getElement(decision.getElementId());
        }

        doc.add(new StoredField(URL_KEY, "/decisions/" + calculateUrl(element, decision)));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DECISION, Field.Store.YES));

        if (element == null) {
            doc.add(new StoredField(NAME_KEY, decision.getId() + ". " + decision.getTitle()));
        } else {
            doc.add(new StoredField(NAME_KEY, decision.getId() + ". " + element.getName() + " - " + decision.getTitle()));
        }

        doc.add(new StoredField(DESCRIPTION_KEY, decision.getStatus()));
        doc.add(new TextField(CONTENT_KEY, appendAll(decision.getTitle(), decision.getContent(), decision.getStatus()), Field.Store.NO));
        indexWriter.addDocument(doc);
    }

    private String calculateUrl(Element element, Decision decision) throws Exception {
        if (element == null) {
            return "*#" + urlEncode("" + decision.getId());
        } else {
            return urlEncode(element.getName()) + "#" + urlEncode(decision.getId());
        }
    }

    protected String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
    }

    protected String calculateUrl(Element element, Section section) throws Exception {
        if (element == null) {
            return "*#" + section.getOrder();
        } else {
            while (element.getParent() != null) {
                element = element.getParent();
            }

            return urlEncode(element.getName()) + "#" + section.getOrder();
        }
    }

    private String appendAll(String... strings) {
        StringBuilder buf = new StringBuilder();

        for (String s : strings) {
            if (!StringUtils.isNullOrEmpty(s)) {
                buf.append(s);
                buf.append(" ");
            }
        }

        return buf.toString();
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        List<SearchResult> results = new ArrayList<>();

        if (workspaceIds.isEmpty()) {
            return results;
        }

        try {
            BooleanQuery.Builder workspaceQueryBuilder = new BooleanQuery.Builder();
            for (Long workspaceId : workspaceIds) {
                workspaceQueryBuilder.add(new TermQuery(new Term(WORKSPACE_KEY, toString(workspaceId))), BooleanClause.Occur.SHOULD);
            }

            StandardAnalyzer analyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser("content", analyzer);
            qp.setDefaultOperator(QueryParser.Operator.AND);

            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder()
                    .add(qp.parse(query), BooleanClause.Occur.MUST)
                    .add(workspaceQueryBuilder.build(), BooleanClause.Occur.MUST);

            if (!StringUtils.isNullOrEmpty(type)) {
                queryBuilder.add(new TermQuery(new Term(TYPE_KEY, type)), BooleanClause.Occur.MUST);
            }

            IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDirectory.toPath()));
            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs = searcher.search(queryBuilder.build(), 20);

            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }

            for (Document doc : documents) {
                SearchResult result = new SearchResult(
                        Long.parseLong(doc.get(WORKSPACE_KEY)),
                        doc.get(URL_KEY),
                        doc.get(NAME_KEY),
                        doc.get(DESCRIPTION_KEY),
                        doc.get(TYPE_KEY)
                );
                results.add(result);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return results;
    }

    private String toString(long workspaceId) {
        return "" + workspaceId;
    }

}