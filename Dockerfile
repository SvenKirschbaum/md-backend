FROM maven:3.9.12-amazoncorretto-25@sha256:d50c14a6772515e2e629860052cc5a4b289844e74fd7136722d88b7220796be7 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.2-alpine@sha256:afb37b0939cf8e627e7a18569b661cd3470611e65639d128f7a709d65615482e

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
