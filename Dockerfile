FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG MODULE_PATH

WORKDIR /workspace

COPY . .

RUN test -n "${MODULE_PATH}"
RUN mvn -pl "${MODULE_PATH}" -am clean package -DskipTests
RUN cp "$(find "${MODULE_PATH}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' | head -n 1)" /tmp/app.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /tmp/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
