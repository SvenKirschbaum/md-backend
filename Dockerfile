FROM maven:3.9.9-amazoncorretto-21@sha256:4e032073ff62f7b35211ebe4546aa9f93916de3ee2a9a4832b510b6e081bfcb0 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.6-alpine@sha256:1b53a05c5693b5452a0c41a39b1fa3b8e7d77aa37f325acc378b7928bc1d8253

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
