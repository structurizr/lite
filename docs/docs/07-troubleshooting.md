## Troubleshooting

#### Structurizr Lite loads, but doesn't look right

If you can see Structurizr Lite in your web browser, but it doesn't look right (e.g. the styles don't seem to be loading, images are oversized, etc), you will likely need to do the following. This is particularly applicable if you're running Structurizr Lite on a server with multiple hostnames, or behind a reverse proxy/load balancer/etc.

1. Create a file named `structurizr.properties` in your Structurizr data directory, next to your `workspace.dsl` file.
2. Add a line as follows: `structurizr.url=XXX` (where `XXX` represents the top-level URL of your Structurizr Lite installation; e.g. `https://structurizr.example.com`).
3. Restart Structurizr Lite.

#### Cannot run program "dot"

If you see a message in the console/logs saying `Cannot run program "dot"`, it means that Graphviz is not installed.
You will either need to install Graphviz, or switch all views in your workspace to not use automatic layout.