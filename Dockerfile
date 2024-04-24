FROM eclipse-temurin:21.0.2_13-jre-jammy
ENV PORT 8080

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD build/libs/structurizr-lite.war /usr/local/structurizr-lite.war

EXPOSE ${PORT}

HEALTHCHECK CMD curl --fail http://localhost:${PORT}/health || exit 1

CMD ["java", "-Djdk.util.jar.enableMultiRelease=false", "-Dserver.port=${PORT}", "-jar", "/usr/local/structurizr-lite.war"]
