FROM maven:3.9.9-amazoncorretto-21@sha256:a6d5113fc9939b50f6183825552672832da44cad98408c02e413f4500ad6599b as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.6-alpine@sha256:c279def074909ec69f5dc76cf1b0ed0370a8a9dd87eb804f8ab6e6b0443aa2fc

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
