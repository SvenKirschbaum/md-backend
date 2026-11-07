FROM maven:3.10.0-amazoncorretto-25@sha256:91d93bc90cf6f488553502ffe494a12136aca91babd8ba4caa10a4e306c0d901 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.5-alpine@sha256:abed4fe0730b802823f0cbc5c24ab8e3ba6023af99266f0e2ca3c08d9f6a20f9

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
