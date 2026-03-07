FROM maven:3.9.12-amazoncorretto-25@sha256:a8e537fe5eba990b340f0d24b1a5399d6dc15323e8eb25e6eed11e5191723ade as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.2-alpine@sha256:afb37b0939cf8e627e7a18569b661cd3470611e65639d128f7a709d65615482e

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
