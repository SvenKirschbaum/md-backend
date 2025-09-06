FROM maven:3.9.11-amazoncorretto-21@sha256:8945baf6139e3fea1cc8518c1cd19ce75a891ae2451a2afd37b39281470d3d9e as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.8-alpine@sha256:fda60fd7965970ce7ed7ce789b18418647b56ac6112fc17df006337bdc6355c4

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
