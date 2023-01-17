## Usage

Navigating to [http://localhost:8080](http://localhost:8080) should open Structurizr Lite,
where you'll be able to view any diagrams, documentation, and decision records defined in your workspace.
Any changes made to the `workspace.dsl` or `workspace.json` files will be reflected when you refresh your web browser.

### Workspace path

Setting an environment variable named `STRUCTURIZR_WORKSPACE_PATH` provides a way to tell Structurizr Lite that your
workspace definition can be found in a subdirectory of the Structurizr data directory.
This can be useful in conjunction with the workspace extension feature; for example:

- `/Users/simon/structurizr/system-landscape.dsl`
- `/Users/simon/structurizr/software-system-1/workspace.dsl` (`extends ../system-landscape.dsl`)
- `/Users/simon/structurizr/software-system-2/workspace.dsl` (`extends ../system-landscape.dsl`)

To start Structurizr Lite against the workspace for "software system 1", with Docker:

```
docker run -it --rm -p 8080:8080 -v /Users/simon/structurizr:/usr/local/structurizr -e STRUCTURIZR_WORKSPACE_PATH=software-system-1 structurizr/lite
```

Or with Spring Boot:

```
export STRUCTURIZR_WORKSPACE_PATH=software-system-1
java -jar structurizr-lite.war /Users/simon/structurizr
```

### Workspace filename

By default, Structurizr Lite will look for files named `workspace.dsl` and `workspace.json` in your Structurizr data directory.
You can customise this behaviour via an environment variable named `STRUCTURIZR_WORKSPACE_FILENAME`.
For example, the following command will look for files named `system-landscape.dsl` and `system-landscape.json` instead:

```
docker run -it --rm -p 8080:8080 -v /Users/simon/structurizr:/usr/local/structurizr -e STRUCTURIZR_WORKSPACE_FILENAME=system-landscape structurizr/lite
```

Please note that you do not need to include the `.dsl` or `.json` file extension.

### Auto-save

By default, auto-save is enabled, with a 5000ms interval. To change this, create a file named `structurizr.properties`
in your Structurizr Lite data directory and add the following line, changing the number of milliseconds as required. A value of `0` will disable auto-save.

```
structurizr.autoSaveInterval=5000
```

### Auto-refresh

By default, auto-refresh is disabled, and changes to your workspace will not be seen in your web browser until you refresh the diagrams/documentation/decision pages.
To enable auto-refresh, create a file named `structurizr.properties`
in your Structurizr Lite data directory and add the following line, changing the number of milliseconds as required. A value of `0` will disable auto-refresh.

```
structurizr.autoRefreshInterval=2000
```

When enabled, Structurizr Lite will look for changes to files inside your Structurizr data directory, and the diagrams/documentation/decision pages will automatically refresh when changes are detected.

### Read-only diagrams

By default, diagrams are editable. To disable this behaviour and make the diagrams read-only, create a file named `structurizr.properties`
in your Structurizr Lite data directory and add the following line.

```
structurizr.editable=false
```