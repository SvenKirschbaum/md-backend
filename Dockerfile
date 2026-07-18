FROM maven:3.9.16-amazoncorretto-25@sha256:4de04d5fe425efd2a5c21ea6c3c53f9f2c4c1381f1d7890d203d237c83fbc816 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.3-alpine@sha256:32d81edae73e1670244827c2f12e5bcf0d335f035b538455fe9d02eb0771d41b

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
