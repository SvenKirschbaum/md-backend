FROM maven:3.9.11-amazoncorretto-25@sha256:f0f3cfc7527bc7575d14ccfa889cba64bb45dd7b29cec35a7a60d2669418f779 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.1-alpine@sha256:e36ee3b9b909ea19d98d7325860bccf286ee519c50c8d33d91cfc47805ff0be7

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
