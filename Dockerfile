FROM maven:3.9.8-amazoncorretto-21@sha256:fa17d738d0907b4d0c7197b17925e55a5d4afb8f923d007fcd38058eb27030ca as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.3-alpine@sha256:ec774b927da7cf72c74c6a54af3f2ccf6993eb6b02ad5e4eabc74b6d2d637300

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
