# Optimize the JVM Docker image size of your Spring Boot applications

## Overview
Java-based container images have the issue of having a considerable size even if the application is quite simple.
When we work with the cloud, a big image can increase deployment time, require more resources, and even lead to higher costs.
The following repository contains the Book Catalog web service, a simple Spring Boot application, and the instructions to
reduce the JVM Docker image size generated by the Dockerfile. 

## App description
The Book catalog web service's tech stack is the following:
* Java 17
* Spring Boot 3.2.4
* H2 in memory database (see the file application.yaml for the details)
* JUnit Jupiter and Mockito for the unit testing

The OpenAPI Specification is used for describing HTTP APIs: [swagger](http://localhost:8080/swagger-ui.html#/)

## Requirements
* JDK 17
* Maven
* A [Docker deamon](https://www.docker.com/products/docker-desktop/) runnig locally

## Guidelines
1. Clone the repository
2. Start the Spring Boot application
3. Play with the fantastic endpoints exposed by the Book Catalog service :-)
4. Containerize the application by copying in the Dockerfile the content of:
    - Dockerfile_full
    - Dockerfile_slim
5. Run the container on the port 8080
   
## Dockerfiles details
As mentioned in the previous section, there are 2 different Dockerfiles (Dockerfile_full, Dockerfile_slim): the goal is
create JVM docker with both of them and checkout the results.
Even if we use a minimal base image (the official [eclipse-temurin:17-alpine](https://hub.docker.com/_/alpine)) and we use the multi-stage build,
the generated JVM Docker image is still quite big for a simple application like the Book Catalog service.
Here is where [jlink](https://docs.oracle.com/en/java/javase/11/tools/jlink.html) comes into the game. Let's see the details in the following section.


### Dockerfile_full
The basic optimization we can put in place for creating slim JVM Docker images for our Spring Boot application is to use
the [multi-stage build](https://docs.docker.com/build/building/multi-stage/). When we need to containerize a Spring Boot application,
we need to build and package the application, and we
also need a JRE to run the application packaged in the .jar file.

The approach described in this example contains 2 stages:
* Build and package the application
* Copy the built application into a JRE

Multi-stage build can provide multiple benefits. The most relevant in the context of this example are:
* Smaller image size: the image contains only essential runtime components
* Faster build time, exploiting the advantages of Docker’s layer caching capabilities

To take even more advantage, we use the [Alpine Docker Official Image](https://www.docker.com/blog/how-to-use-the-alpine-docker-official-image/),

Finally, the Dockerfile looks like:
```
#Multi-stage build

#Stage 1: initialize build and set base image for first stage
FROM maven:3.8.8-amazoncorretto-17 AS build

WORKDIR /opt/app/

COPY pom.xml .
RUN mvn dependency:go-offline

COPY ./src ./src

# compile the source code and package it in a jar file
RUN mvn clean install -DskipTests

#Stage 2: set base image for second stage
FROM eclipse-temurin:17-alpine

# set deployment directory
WORKDIR /opt/app/

COPY --from=build /opt/app/target/bookCatalog-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "api"]
```
Using this approach, we got an overall Docker image size of about 370 MB.

That's not enough? We can do better.

### Dockerfile_slim
Java 9 introduced a new level of abstraction above packages, the “Modules”. A [module](https://www.oracle.com/corporate/features/understanding-java-9-modules.html)
is a uniquely named, reusable group of related packages. Java modularization help us to “build” our own JDK. 

We're going to use 2 tools that have been added to the JDK:
1. [Jdeps](https://docs.oracle.com/en/java/javase/11/tools/jdeps.html), the Java class dependency analyzer
2. [Jlink](https://docs.oracle.com/en/java/javase/11/tools/jlink.html), the tool to assemble and optimize a set of modules
   and their dependencies into a custom runtime image.

The main issue with Spring Boot is that it creates a fat JAR for out application containing all the dependencies.
There are multiple ways to determine which modules the application needs and all its dependencies: in this example we extract
all the dependencies by using jar xf command. Once extracted, the dependencies are used by Jdeps.

The Jdeps analysis ends up with the following dependencies:
```
java.base, java.compiler, java.datatransfer, java.desktop, java.instrument, java.logging, java.management,
java.naming, java.net.http, java.prefs, java.rmi, java.scripting, java.security.jgss , java.security.sasl
java.sql, java.sql.rowset, java.transaction.xa, java.xml, jdk.jfr, jdk.net, jdk.unsupported
```
Our custom JRE will be based on them.

The complete Dockerfile looks like:

```
# Custom jre build
FROM maven:3.8.8-amazoncorretto-17 AS build

COPY . /opt/app/
WORKDIR /opt/app/

RUN mvn package -DskipTests

# Extract the application dependencies
RUN jar xf target/bookCatalog-0.0.1-SNAPSHOT.jar

# Analyze the dependencies contained into the fat jar
RUN jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 17  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    target/bookCatalog-0.0.1-SNAPSHOT.jar > deps.info

# Create the custom JRE
RUN jlink \
    -- verbose \
    --add-modules $(cat deps.info) \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /custom_jre

# Use the custom jre as a base image
FROM debian:buster-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=build /custom_jre $JAVA_HOME

COPY --from=build /opt/app/target/bookCatalog-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "api"]
```
This time we got an overall Docker image size of about 190 MB. Compared to the previous result, our image weights half!
## Conclusions
We often struggle with the size of the Docker images when deploying Java applications in Docker containers. Starting from
Java 9 we can use Jlink, the useful tool to assemble and optimize a set of modules
and their dependencies into a custom runtime image.
Working with smaller Docker images brings several benefits:
* financial: our applications require less space
* performance: containers start-up is faster
* security: images that contain fewer artifacts are more secure

| ![spring-boot-docker-images.png](..%2F..%2FUsers%2FMarco%2FDesktop%2Fmodules_project%2Fspring-boot-docker-images.png) | 
|:---------------------------------------------------------------------------------------------------------------------:| 
|                     *The comparison between the two Docker images obtained with our Dockerfiles*                      |