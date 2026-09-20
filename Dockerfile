FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG MODULE_PATH

WORKDIR /workspace

COPY . .

RUN test -n "${MODULE_PATH}"
RUN mvn -pl "${MODULE_PATH}" -am clean package -DskipTests
RUN set -eu; \
    set -- "${MODULE_PATH}"/target/*-exec.jar; \
    if [ ! -f "$1" ]; then \
        set -- "${MODULE_PATH}"/target/*.jar; \
    fi; \
    if [ "$#" -ne 1 ] || [ ! -f "$1" ]; then \
        echo "Expected exactly one executable JAR for ${MODULE_PATH}" >&2; exit 1; \
    fi; \
    cp "$1" /tmp/app.jar; \
    mkdir -p /tmp/jar-manifest; \
    cd /tmp/jar-manifest; \
    jar xf /tmp/app.jar META-INF/MANIFEST.MF; \
    grep -q '^Main-Class: ' META-INF/MANIFEST.MF

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /tmp/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
