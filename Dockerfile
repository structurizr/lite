FROM eclipse-temurin:21.0.6_7-jre-noble AS structurizr-lite
ENV PORT=8080

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD build/libs/structurizr-lite.war /usr/local/structurizr-lite.war

EXPOSE ${PORT}

HEALTHCHECK CMD curl --fail http://localhost:${PORT}/health || exit 1

CMD ["java", "-Dserver.port=${PORT}", "-jar", "/usr/local/structurizr-lite.war"]

FROM structurizr-lite AS structurizr-export

ENV STRUCTURIZR_EXPORT=/opt/structurizr-export
ENV STRUCTURIZR_WORKSPACE_PATH=/workspace

RUN set -eux; \
	apt-get install -y --no-install-recommends git nodejs npm; \
        git clone https://github.com/structurizr/puppeteer.git $STRUCTURIZR_EXPORT; \
        sed -i $STRUCTURIZR_EXPORT/export-diagrams.js -e 's@headless: HEADLESS@headless: HEADLESS, args: [ "--no-sandbox" ]@'; \
        sed -i $STRUCTURIZR_EXPORT/export-documentation.js -e 's@headless: HEADLESS@headless: HEADLESS, args: [ "--no-sandbox" ]@'; \
	cd $STRUCTURIZR_EXPORT; \
        npm install puppeteer; \
	npx puppeteer browsers install --install-deps chrome-headless-shell@stable; \
	mkdir $STRUCTURIZR_WORKSPACE_PATH; \
        apt-get remove -y --purge git npm; \
        apt-get autoremove -y; \
	rm -rf /var/lib/apt/lists/* /var/cache/apt/*

COPY --chmod=0775 puppeteer-export-diagrams.sh /usr/local/bin/export-diagrams
COPY --chmod=0775 puppeteer-export-documentation.sh /usr/local/bin/export-documentation

WORKDIR /workspace

CMD ["/bin/bash"]
