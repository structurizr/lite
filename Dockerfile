FROM eclipse-temurin:17.0.4.1_1-jre-jammy

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends graphviz

ADD build/libs/structurizr-lite.war /usr/local/structurizr-lite.war

EXPOSE 8080

CMD ["java", "-jar", "/usr/local/structurizr-lite.war"]