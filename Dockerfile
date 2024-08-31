FROM maven:3.9.9-amazoncorretto-21@sha256:87d9a02662cb7816f9dd14d0eaf5676135eb2e7352647d07dfe173048c775f12 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.4-alpine@sha256:4cff3d338418faa41a20bf384c7ed67b8a6897ce5ce0e3fecd7ea08c5c7a2909

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
