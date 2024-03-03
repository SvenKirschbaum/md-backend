FROM maven:3.9.6-amazoncorretto-21@sha256:163c98495b9d4406d956135cf6e8bb5fd018fa14717b412643820c7434811808 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.2-alpine@sha256:cc4c1d0cab18894f4470b3afda995fbda8b5166d9d646205a18357b2b20c4b2b

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
