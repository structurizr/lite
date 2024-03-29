FROM eclipse-temurin:21.0.1_12-jre-jammy
ENV PORT 8080

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD build/libs/structurizr-lite.war /usr/local/structurizr-lite.war

EXPOSE ${PORT}

CMD ["java", "-Djdk.util.jar.enableMultiRelease=false", "-Dserver.port=${PORT}", "-jar", "/usr/local/structurizr-lite.war"]
