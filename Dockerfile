FROM maven:3.9.9-amazoncorretto-21@sha256:85c4ff026a5d14afb8db6c7bdcfd154fb632108047e6b0e677beeb3247eed23a as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.5-alpine@sha256:7ab62108b2a065f6fb42636aaf6d0b408b551d3c31c9c8a8734410abb09064ba

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
