FROM maven:3.10.0-amazoncorretto-25@sha256:ccc394dd4226c146c2e81f68b3d95e68227e9ffaa88fe04b9f22fd6552190e0b as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.5-alpine@sha256:e3ec06bdc025f54f1dbeeb92a8cc537665371badec999ce832c1cb6c00a2c828

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
