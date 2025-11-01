FROM maven:3.9.11-amazoncorretto-25@sha256:5f951de7547a7bf46ca2bcdc9d0469b0dbe50760b7fe373e32fc5c894c51e2ea as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.1-alpine@sha256:e36ee3b9b909ea19d98d7325860bccf286ee519c50c8d33d91cfc47805ff0be7

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
