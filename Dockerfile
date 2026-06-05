FROM maven:3.9.16-amazoncorretto-25@sha256:d07e29323836507efd1c05eaae5114101386abd48c768d5b3ea34389f4555eb8 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.3-alpine@sha256:80667e38af71ac103a3ae36a0b531d54c73c4da28fc02b57f69bce8993c0e1b0

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
