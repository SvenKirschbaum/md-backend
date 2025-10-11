FROM maven:3.9.11-amazoncorretto-25@sha256:24275bc4a714ab6f148fdcdf4d1a1207ddf7f4dba163f282f6f81a6cf9e4eeb1 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.0-alpine@sha256:e779e964a15d62c8c39dd3faa17ed2aa921795b642d4437c6c8a3ad8d581cf36

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
