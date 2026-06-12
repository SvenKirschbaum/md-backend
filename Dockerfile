FROM maven:3.9.16-amazoncorretto-25@sha256:8f1387c7eef62aed06070a80971bd0c17e3f3b90ce531294110ad25068b6ae6f as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.3-alpine@sha256:d0915ce12f1c011cc332c32ea21ec8ad85ef47b03e46f1a4bfa8c5cd602468a4

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
