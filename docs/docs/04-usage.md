## Usage

Structurizr Lite will look for a `workspace.dsl` and `workspace.json` file in your Structurizr data directory, in that order, and use the file it finds first.
Navigating to [http://localhost:8080](http://localhost:8080) should open Structurizr Lite,
where you'll be able to view any diagrams, documentation, and decision records defined in your workspace.
Any changes made to the `workspace.dsl` or `workspace.json` file will be reflected when you refresh your web browser.

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

### Auto-sync

Structurizr Lite's auto-sync feature allows you to automatically sync Structurizr Lite with a remote workspace from the Structurizr cloud service or an on-premises installation
(excluding on-premises installations with self-signed certificates; support for this may be added in the future).
This means that you can use Structurizr Lite for local authoring, and publish that same workspace for other people to view.

#### Configuring auto-sync

To configure auto-sync, create a file named `structurizr.properties`
in your Structurizr Lite data directory. The contents of the file should be as follows;
replacing `ID`, `KEY`, and `SECRET` with your remote workspace ID, API key, and API secret respectively (you can find these on your workspace settings page).


```
structurizr.remote.workspaceId=ID
structurizr.remote.apiKey=KEY
structurizr.remote.apiSecret=SECRET
```


A property named `structurizr.remote.apiUrl` can be used to specify an API URL for on-premises installations (this is not required for the cloud service).
A property named `structurizr.remote.passphrase` can be used to specify the [client-side encryption](https://structurizr.com/help/client-side-encryption) passphrase.


#### Using auto-sync

With auto-sync configured, Structurizr Lite will pull a copy of your remote workspace upon startup and save it as `workspace.json`
(unless the local version is newer than the remote version) - this includes any diagram layout information, which you will now see in Lite.
You can then edit your workspace as usual (e.g. by modifying your `workspace.dsl` file, saving it, and refreshing your web browser).
When you've finished making edits, ensure that any local diagram layout changes are saved in Structurizr Lite's diagram editor.
When you shutdown Structurizr Lite, it will automatically push this new version to your cloud service/on-premises installation.
Opening your remote workspace will confirm that all changes have been synced.


#### Workspace locking

If you'd like to use Structurizr Lite in conjunction with the cloud service/on-premises workspace locking feature,
open the diagram editor for your remote workspace to lock the workspace before starting Structurizr Lite.
You will need to leave this browser window/tab open for the lock to remain active.
When starting Structurizr Lite, provide an environment variable to the Docker container named `STRUCTURIZR_USERNAME`,
with a value of your remote Structurizr username (for the cloud service, this is the e-mail address that you sign in with).
You can then use Lite as above, shutting it down to sync the workspace as normal.

When enabled, Structurizr Lite will look for changes to files inside your Structurizr data directory, and the diagrams/documentation/decision pages will automatically refresh when changes are detected.

### Read-only diagrams

By default, diagrams are editable. To disable this behaviour and make the diagrams read-only, create a file named `structurizr.properties`
in your Structurizr Lite data directory and add the following line.

```
structurizr.editable=false
```