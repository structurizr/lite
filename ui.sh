# - this is a temporary script that merges the contents of the structurizr/ui repository into this directory,
# - it will likely be migrated in the Gradle build file at some point in the future
# - this has only been tested on MacOS

export STRUCTURIZR_UI_DIR=../structurizr-ui
export STRUCTURIZR_LITE_DIR=.

mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static

# JavaScript
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js
cp $STRUCTURIZR_UI_DIR/src/js/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js

# CSS
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css
cp $STRUCTURIZR_UI_DIR/src/css/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css

# CSS fonts
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css/fonts
cp $STRUCTURIZR_UI_DIR/src/css/fonts/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css/fonts

# Images
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/img
cp $STRUCTURIZR_UI_DIR/src/img/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/img

# Bootstrap icons
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/bootstrap-icons
cp $STRUCTURIZR_UI_DIR/src/bootstrap-icons/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/bootstrap-icons

# HTML (offline exports)
mkdir $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/html
cp $STRUCTURIZR_UI_DIR/src/html/* $STRUCTURIZR_LITE_DIR/src/main/resources/static/static/html

# JSP fragments
cp $STRUCTURIZR_UI_DIR/src/fragments/* $STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/fragments
mkdir $STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/fragments/workspace
cp $STRUCTURIZR_UI_DIR/src/fragments/workspace/* $STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/fragments/workspace

# JSP
cp $STRUCTURIZR_UI_DIR/src/jsp/* $STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/jsp

