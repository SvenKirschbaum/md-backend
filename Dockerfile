FROM maven:3.10.0-amazoncorretto-25@sha256:bf28f1ea992519d0c6a94e970b64ef97a3398795be8081f86ebd66786f94abdd as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.4-alpine@sha256:19f1e2198abaaf201f5b9faa39222412da3fad66415e9dfe253bd6763415097e

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
