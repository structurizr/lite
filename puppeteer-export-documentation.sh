#!/bin/bash
# export <output-directory> [[format] ...]

cleanup() {
    kill %1
    wait
}

export STRUCTURIZR_WORKSPACE_FILENAME=$1
shift
OUTPUT_DIR="${STRUCTURIZR_WORKSPACE_PATH}/${1}"
shift

java -Dserver.port=${PORT} -jar /usr/local/structurizr-lite.war / &
(until printf "" 2>>/dev/null >>/dev/tcp/localhost/$PORT; do sleep 1; done) > /dev/null 2>&1

trap cleanup EXIT
trap cleanup INT
trap cleanup TERM

mkdir -p "$OUTPUT_DIR"
cd "$OUTPUT_DIR"

echo exporting $STRUCTURIZR_WORKSPACE_FILENAME documentation in $OUTPUT_DIR
node $STRUCTURIZR_EXPORT/export-documentation.js http://localhost:$PORT $FORMAT
