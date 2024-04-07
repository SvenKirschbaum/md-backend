FROM maven:3.9.6-amazoncorretto-21@sha256:3a69a5d9c136b0d8812f45bf07ca3830dd19908126a452fefcd51dab19c8e658 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.2-alpine@sha256:b2057d2b22c9abf559ea3c0334a161fb0e615bbcf27713be2b754fd2ca804526

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
