FROM eclipse-temurin:21.0.8_9-jre-noble
ENV PORT=8080

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD build/libs/structurizr-lite.war /usr/local/structurizr-lite.war

EXPOSE ${PORT}

HEALTHCHECK CMD curl --fail http://localhost:${PORT}/health || exit 1

CMD ["java", "-Dserver.port=${PORT}", "-jar", "/usr/local/structurizr-lite.war"]
