FROM maven:3.9.16-amazoncorretto-25@sha256:98295c180adc4b5c0a52b830e00c387c862d5827d395cd7737d8205170428785 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.4-alpine@sha256:2ad5f5cf03a3970f2478b130dc28f51b179ce13c58154fe3ec1a6fdeb3b86e3a

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
