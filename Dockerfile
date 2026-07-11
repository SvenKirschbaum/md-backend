FROM maven:3.9.16-amazoncorretto-25@sha256:10a4c5d155c12014589a4d474901c36f1258f3f066fea2317a7722f42c6a2a25 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.3-alpine@sha256:32d81edae73e1670244827c2f12e5bcf0d335f035b538455fe9d02eb0771d41b

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
