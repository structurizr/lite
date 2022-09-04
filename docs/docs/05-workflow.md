## Workflow

The recommended workflow depends on whether you're using the DSL vs a code-based client library,
and automatic vs manual layout for the diagrams in your workspace.
Structurizr Lite is optimised for working with the DSL, and this is the recommended approach for most teams.
Unlike the cloud service and on-premises installation, Structurizr Lite does not require you to push your workspace to it via a web API.
Instead it works directly off the file system.

### Structurizr DSL

For local authoring:

1. Create your workspace using the DSL, as a file named `workspace.dsl` in your Structurizr Lite data directory. Any included files (documentation, ADRs, icons, etc) should also be accessible from this directory.
2. Start Structurizr Lite.
3. Open your web browser and navigate to Structurizr Lite (e.g. [http://localhost:8080](http://localhost:8080)) to see your workspace content.
4. Modify the diagram layout as needed, and save your workspace via the web UI (the "Save workspace" button). Structurizr Lite will write a JSON version of your workspace into your Structurizr Lite data directory to a file named `workspace.json` - this includes your workspace and the layout information for your diagrams.
5. Make any changes as required to your DSL, and save your workspace.
6. Refresh your web browser to see the changes.

To publish your local workspace to the Structurizr cloud service or an on-premises installation:

- For workspaces where all diagrams are configured to use auto-layout, just push the `workspace.dsl` file using the [Structurizr CLI](https://github.com/structurizr/cli).
- For workspaces where one or more diagrams are configured to use manual layout, push the `workspace.json` file using the [Structurizr CLI](https://github.com/structurizr/cli) with `-merge false` to force your local diagram layout to be used.
- Alternatively you can configure [auto-sync](04-auto-sync.md).

### Code-based client library

For local authoring, create your workspace using code and, rather than pushing it via the web API, export the workspace to a file named `workspace.json` in your Structurizr Lite data directory. With the Java client library, this can be achieved with the following code, which will ensure that any diagram layout changes created by Structurizr Lite are retained when regenerating your workspace from code:

```
Workspace workspace = new Workspace(...);
// create the workspace

File file = new File("/some/path/workspace.json");
if (file.exists()) {
    Workspace liteWorkspace = WorkspaceUtils.loadWorkspaceFromJson(file); // load the old version that contains layout information
    workspace.getViews().copyLayoutInformationFrom(liteWorkspace.getViews()); // copy layout information into the new workspace
}
WorkspaceUtils.saveWorkspaceToJson(workspace, file); // save the new workspace
```

To publish your local workspace to the Structurizr cloud service or an on-premises installation, push the `workspace.json` file using the [Structurizr CLI](https://github.com/structurizr/cli) or your client library.