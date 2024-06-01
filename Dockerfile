FROM maven:3.9.7-amazoncorretto-21@sha256:b2b579d67899646bcaa4a8341ca2045122cb3abab128121448a5bf24ace1d791 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.3-alpine@sha256:7e522a694566c0c6cd80b06d97bc69f4be31a518d81d6cdd30c9a854a56aa84a

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
