FROM maven:3.9.9-amazoncorretto-21@sha256:92aba821b989f5863d55f65ccee20f61161e482a8e4555997e94288e584936c0 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.5-alpine@sha256:8b16834e7fabfc62d4c8faa22de5df97f99627f148058d52718054aaa4ea3674

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
