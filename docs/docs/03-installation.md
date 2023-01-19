## Installation

Structurizr Lite is available as [Docker image](#docker) (recommended) and [a Spring Boot application](#spring-boot).

### Create the Structurizr data directory

Structurizr Lite needs to be given access to a directory, in which a `workspace.dsl` or `workspace.json` file exists that defines your workspace.
We'll refer to this directory as the "Structurizr data directory".
If this directory doesn't exist when Structurizr Lite is started up, it will be created for you, along with a basic DSL file.

### Docker

Assuming that you have Docker installed, to start Structurizr Lite, use the following command to pull the image from [Docker Hub](https://hub.docker.com/r/structurizr/lite).

```
docker pull structurizr/lite
```

Then use the following command to start the Docker container, replacing `PATH` with the path to your Structurizr data directory:

```
docker run -it --rm -p 8080:8080 -v PATH:/usr/local/structurizr structurizr/lite
```

For example, if your Structurizr data directory is located at `/Users/simon/structurizr`, the command would be:

```
docker run -it --rm -p 8080:8080 -v /Users/simon/structurizr:/usr/local/structurizr structurizr/lite
```

### Spring Boot

To use the Spring Boot version, you'll need Java 11+ (required) and [Graphviz](https://graphviz.org/download/) (optional; if you want to use automatic layout).
Download the Spring Boot .war file from [https://static.structurizr.com/download/structurizr-lite.war](https://static.structurizr.com/download/structurizr-lite.war), and start with the following command, replacing `PATH` with the path to your Structurizr data directory:

```
java -jar structurizr-lite.war PATH
```


For example, if your Structurizr data directory is located at `/Users/simon/structurizr`, the command would be:

```
java -jar structurizr-lite.war /Users/simon/structurizr
```

#### Building your own Docker image

There is a [Dockerfile](https://github.com/structurizr/lite/blob/main/Dockerfile) in the GitHub repo that can be used as a starting point if you'd like to build your own Docker image.