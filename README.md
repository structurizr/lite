![Structurizr](https://static.structurizr.com/img/structurizr-banner.png)

# Structurizr Lite

This version of Structurizr allows you to view/edit diagrams, view documentation, and view architecture decision records defined in a single DSL or JSON workspace.
Structurizr Lite can be used standalone for workspace viewing/authoring, or in conjunction with the cloud service/on-premises installation as an authoring/testing/staging environment.

Structurizr Lite will look for a `workspace.dsl` and `workspace.json` file in a given directory, in that order, and use the file it finds first.
If you change this file (e.g. via your text editor or one of the Structurizr client libraries), you can refresh your web browser to immediately see the changes.

- [Getting started](https://structurizr.com/share/76352/documentation#getting-started)
- [Documentation](https://structurizr.com/share/76352/documentation) ([source](docs))
- [Issue tracker](https://github.com/structurizr/lite/issues)

## Building from source

Open sourcing Structurizr Lite is a work in progress, and should be completed later in 2023.
Much of the UI code (HTML, CSS, JS, JSP, etc) is shared with the Structurizr cloud service and on-premises installation,
and hasn't been fully open sourced yet.
This means you will not be able to build a fully working version of Structurizr Lite from this repo.

❌ Diagrams
✅ Graph view
✅ Documentation
✅ Decisions

To build from source (this has only been tested with Java 17):

```
git clone https://github.com/structurizr/lite.git structurizr-lite
git clone https://github.com/structurizr/ui.git structurizr-ui
cd structurizr-lite
./ui.sh
./gradlew build
```

And to run Structurizr Lite (this has only been tested with Java 17):

```
java -jar build/libs/structurizr-lite.war /path/to/workspace
```

(replace `/path/to/workspace` with the path to the folder where your `workspace.dsl` file is)