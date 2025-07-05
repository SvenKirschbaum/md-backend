FROM maven:3.9.10-amazoncorretto-21@sha256:4e3a3d57ae19c5c80e78050f0e580e2decd869ff81f2c9aaa173d070975a0997 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.7-alpine@sha256:937a7f5c5f7ec41315f1c7238fd9ec0347684d6d99e086db81201ca21d1f5778

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
