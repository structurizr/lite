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

for FORMAT in $@; do
    echo exporting $STRUCTURIZR_WORKSPACE_FILENAME diagrams to $FORMAT in $OUTPUT_DIR
    for FILE in $(node $STRUCTURIZR_EXPORT/export-diagrams.js http://localhost:$PORT $FORMAT | grep "^ - .*\\.$FORMAT" | sed "s/ - //"); do
        OUTPUT_FILE="$OUTPUT_DIR/${STRUCTURIZR_WORKSPACE_FILENAME}_${FILE}"
        mv "$OUTPUT_DIR/$FILE" "$OUTPUT_FILE"
        echo exported "$OUTPUT_FILE"
    done
done
