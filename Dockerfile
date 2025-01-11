FROM maven:3.9.9-amazoncorretto-21@sha256:17ae7b5533254592b8ab1a159cdb63777a692eab49754b708711854c0a68d6a4 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.5-alpine@sha256:b66d1d797ca711a537667d22aea4713577338bd161447c91efaa61a8e6855981

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
