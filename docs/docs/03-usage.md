## Usage

Navigating to [http://localhost:8080](http://localhost:8080) should open Structurizr Lite,
where you'll be able to view any diagrams, documentation, and decision records defined in your workspace.
Any changes made to the `workspace.dsl` or `workspace.json` files will be reflected when you refresh your web browser.

### Workspace extension and offsets
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

