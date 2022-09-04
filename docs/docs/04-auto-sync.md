## Auto-sync

Structurizr Lite's auto-sync feature allows you to automatically sync Structurizr Lite with a remote workspace from the Structurizr cloud service or an on-premises installation
(excluding on-premises installations with self-signed certificates; support for this may be added in the future).
This means that you can use Structurizr Lite for local authoring, and publish that same workspace for other people to view.

### Configuring auto-sync

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


### Using auto-sync

With auto-sync configured, Structurizr Lite will pull a copy of your remote workspace upon startup and save it as `workspace.json`
(unless the local version is newer than the remote version) - this includes any diagram layout information, which you will now see in Lite.
You can then edit your workspace as usual (e.g. by modifying your `workspace.dsl` file, saving it, and refreshing your web browser).
When you've finished making edits, ensure that any local diagram layout changes are saved in Structurizr Lite's diagram editor.
When you shutdown Structurizr Lite, it will automatically push this new version to your cloud service/on-premises installation.
Opening your remote workspace will confirm that all changes have been synced.


### Workspace locking

If you'd like to use Structurizr Lite in conjunction with the cloud service/on-premises workspace locking feature,
open the diagram editor for your remote workspace to lock the workspace before starting Structurizr Lite.
You will need to leave this browser window/tab open for the lock to remain active.
When starting Structurizr Lite, provide an environment variable to the Docker container named `STRUCTURIZR_USERNAME`,
with a value of your remote Structurizr username (for the cloud service, this is the e-mail address that you sign in with).
You can then use Lite as above, shutting it down to sync the workspace as normal.
