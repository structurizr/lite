#!/bin/bash

# - this script merges the contents of the structurizr/ui repository into this directory,
# - this has only been tested on WSL

export STRUCTURIZR_BUILD_NUMBER=$1
export STRUCTURIZR_UI_DIR=../structurizr-ui
export STRUCTURIZR_LITE_DIR=.

# Remove existing static resources
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/bootstrap-icons"
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css"
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/html"
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/img"
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js"
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static"

# JavaScript
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js"
cp -a "$STRUCTURIZR_UI_DIR/src/js/"* "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js"

if [[ -n $STRUCTURIZR_BUILD_NUMBER ]]; then
  for file in "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/js/structurizr"*.js; do
    filename="${file%.*}"

    if [[ $file == *structurizr-embed.js ]]; then
      echo "Skipping $filename"
    else
      mv "$filename.js" "$filename-$STRUCTURIZR_BUILD_NUMBER.js"
    fi
  done
fi

# CSS
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css"
cp -a "$STRUCTURIZR_UI_DIR/src/css/"* "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/css"

# Images
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/img"
cp -a "$STRUCTURIZR_UI_DIR/src/img/"* "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/img"

# Bootstrap icons
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/bootstrap-icons"
cp -a "$STRUCTURIZR_UI_DIR/src/bootstrap-icons/"* "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/bootstrap-icons"

# HTML (offline exports)
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/html"
cp "$STRUCTURIZR_UI_DIR/src/html/"* "$STRUCTURIZR_LITE_DIR/src/main/resources/static/static/html"

# JSP fragments
cp -a "$STRUCTURIZR_UI_DIR/src/fragments/"* "$STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/fragments"
rm -rf "$STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/fragments/dsl"

# JSP
cp -a "$STRUCTURIZR_UI_DIR/src/jsp/"* "$STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/jsp"
rm "$STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/jsp/review.jsp"
rm "$STRUCTURIZR_LITE_DIR/src/main/webapp/WEB-INF/jsp/review-create.jsp"

# Java
mkdir -p "$STRUCTURIZR_LITE_DIR/src/main/java/com/structurizr/util/"
cp "$STRUCTURIZR_UI_DIR/src/java/com/structurizr/util/DslTemplate.java" "$STRUCTURIZR_LITE_DIR/src/main/java/com/structurizr/util/"