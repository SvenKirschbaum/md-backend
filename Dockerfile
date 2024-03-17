FROM maven:3.9.6-amazoncorretto-21@sha256:2b884f295f9be15ea747c09ddee8f9a425d06909a6345a4bb8bdda8da2e9974e as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.2-alpine@sha256:b2057d2b22c9abf559ea3c0334a161fb0e615bbcf27713be2b754fd2ca804526

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
