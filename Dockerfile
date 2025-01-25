FROM maven:3.9.9-amazoncorretto-21@sha256:a6d5113fc9939b50f6183825552672832da44cad98408c02e413f4500ad6599b as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.6-alpine@sha256:58c17296dcd293d685e36fa16fb96216c9bcea5c810a879308abc463a2616d44

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
