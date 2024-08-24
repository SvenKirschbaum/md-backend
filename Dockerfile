FROM maven:3.9.9-amazoncorretto-21@sha256:05d9068cdb35a72b08046e52d3ee1e9db3a3b0ae7193e965524bd4e93b38e796 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.4-alpine@sha256:4cff3d338418faa41a20bf384c7ed67b8a6897ce5ce0e3fecd7ea08c5c7a2909

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
