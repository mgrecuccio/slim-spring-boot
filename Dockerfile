FROM maven:3.8.8-amazoncorretto-17 AS build

COPY . /opt/app/
WORKDIR /opt/app/

RUN mvn package -DskipTests
RUN jar xf target/bookCatalog-0.0.1-SNAPSHOT.jar

RUN jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 17  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    target/bookCatalog-0.0.1-SNAPSHOT.jar > deps.info

RUN jlink \
    --add-modules $(cat deps.info) \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /custom_jre

FROM debian:buster-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=build /custom_jre $JAVA_HOME

# Continue with your application deployment
COPY --from=build /opt/app/target/bookCatalog-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "api"]