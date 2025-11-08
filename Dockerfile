FROM maven:3.9.11-amazoncorretto-25@sha256:d822074c1db11474890693cd64df4173e498861c41ba312f1a5244bd53a458e5 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.1-alpine@sha256:e3818f93bee840c1593492ba5335ceb214ffe4a37a8275e49d23aab6f66b9f6a

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
