# Structurizr Lite

This version of Structurizr allows you to view/edit diagrams, view documentation, and view architecture decision records defined in a single DSL or JSON workspace.
Structurizr Lite can be used standalone for workspace viewing/authoring, or in conjunction with the cloud service/on-premises installation as an authoring/testing/staging environment.

Structurizr Lite will look for a `workspace.dsl` and `workspace.json` file in a given directory, in that order, and use the file it finds first.
If you change this file (e.g. via your text editor or one of the Structurizr client libraries), you can refresh your web browser to immediately see the changes.

- [Documentation](https://docs.structurizr.com/lite)
- [Issue tracker](https://github.com/structurizr/lite/issues)

## Building from source

To build and run from source, you'll need:

- Java 17+ (required)
- [Graphviz](https://graphviz.org/download/) (optional; if you want to use automatic layout)

- Note 1: The HTML, JS, CSS, JSP, etc files are in a separate [structurizr/ui](https://github.com/structurizr/ui) repo because they are shared with the on-premises installation and cloud service.
- Note 2: Building and running from source has only been tested with Java 17.

To build from source:

```
git clone https://github.com/structurizr/lite.git structurizr-lite
git clone https://github.com/structurizr/ui.git structurizr-ui
cd structurizr-lite
./ui.sh
./gradlew build
```

And to run Structurizr Lite (this has only been tested with Java 17):

```
java -Djdk.util.jar.enableMultiRelease=false -jar build/libs/structurizr-lite.war /path/to/workspace
```

- Replace `/path/to/workspace` with the path to the folder where your `workspace.dsl` file is.
- See https://github.com/spring-projects/spring-boot/issues/33633 for details of why `-Djdk.util.jar.enableMultiRelease=false` is required.