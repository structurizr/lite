## Troubleshooting

### Structurizr Lite loads, but doesn't look right

If you can see Structurizr Lite in your web browser, but it doesn't look right (e.g. the styles don't seem to be loading, images are oversized, etc), you will likely need to do the following. This is particularly applicable if you're running Structurizr Lite on a server with multiple hostnames, or behind a reverse proxy/load balancer/etc.

1. Create a file named `structurizr.properties` in your Structurizr data directory, next to your `workspace.dsl` file.
2. Add a line as follows: `structurizr.url=XXX` (where `XXX` represents the top-level URL of your Structurizr Lite installation; e.g. `https://structurizr.example.com`).
3. Restart Structurizr Lite.

### Cannot run program "dot"

If you see a message in the console/logs saying `Cannot run program "dot"`, it means that Graphviz is not installed.
You will either need to install Graphviz, or switch all views in your workspace to not use automatic layout.

### Diagram layout is lost when using manual layout

Whenever you refresh Structurizr Lite, it will parse the `workspace.dsl` file and transform it into a workspace.
But that new workspace doesn't include any layout information, because it's not stored in the DSL.
Instead, the layout information is stored in the JSON representation of the workspace, in a file named `workspace.json`.

When the DSL is refreshed, Structurizr Lite will additionally load the `workspace.json` file,
and copy the layout information into the newly created version.
This is done via [a pluggable merging algorithm in the Structurizr for Java library](https://github.com/structurizr/java/blob/master/structurizr-core/src/com/structurizr/view/DefaultLayoutMergeStrategy.java). 
This algorithm works most of the time, but doesn't cater for a number of edge cases unfortunately.

Individual elements losing their layout information is generally caused by renaming those elements, often in conjunction with changing
the order of lines in the DSL, thereby affecting the order in which elements are created, and the internal IDs they are given.
In essence, the merging algorithm isn't able to match elements created via the `workspace.dsl` file with those that already exist
in the `workspace.json` file.

Entire diagrams losing their layout information is generally caused by changing the key associated with that view.
To minimise this, please be sure to use stable keys for your views, rather than allowing the DSL parser to generate them for you.
For example:

```
systemLandscape "MyViewKey" {
  include *
}
```

 Rather than:

```
systemLandscape {
  include *
}
```