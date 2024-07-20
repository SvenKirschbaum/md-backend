FROM maven:3.9.8-amazoncorretto-21@sha256:5625f89880deb3bac2d7122339fc106684effd0c8ad683152ff9e0431290c53a as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.4-alpine@sha256:88ca0023ea680ee8f2da87f30abef7303d1320ee9c9655a93e386aaba81647fa

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
