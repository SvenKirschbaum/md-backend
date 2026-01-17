FROM maven:3.9.12-amazoncorretto-25@sha256:9e11d6243a43126be3418ef46622f2054f9718b7924991c57b4b1cf78552f610 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.1-alpine@sha256:e3818f93bee840c1593492ba5335ceb214ffe4a37a8275e49d23aab6f66b9f6a

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
