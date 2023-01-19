![Structurizr](https://static.structurizr.com/img/structurizr-banner.png)

# Structurizr Lite

This version of Structurizr allows you to view/edit diagrams, view documentation, and view architecture decision records defined in a single DSL or JSON workspace.
Structurizr Lite can be used standalone for workspace viewing/authoring, or in conjunction with the cloud service/on-premises installation as an authoring/testing/staging environment.

Structurizr Lite will look for a `workspace.dsl` and `workspace.json` file in a given directory, in that order, and use the file it finds first.
If you change this file (e.g. via your text editor or one of the Structurizr client libraries), you can refresh your web browser to immediately see the changes.

- [Getting started](https://structurizr.com/share/76352/documentation#getting-started)
- [Documentation](https://structurizr.com/share/76352/documentation) ([source](docs))
- [Issue tracker](https://github.com/structurizr/lite/issues)

## Important note

Open sourcing Structurizr Lite is a work in progress, and should be completed later in 2023.
Most of the UI code (HTML, CSS, JS, JSP, etc) is shared with the Structurizr cloud service and on-premises installation, so isn't yet available in this repo.
This means you will not be able to build a fully working version of Structurizr Lite from this repo.