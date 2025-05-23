FROM maven:3.9.9-amazoncorretto-21@sha256:e5d25f2982a71d7637e6a00602afeb2bed7ec3a7da0afd96211f09dfa531d2ed as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.7-alpine@sha256:937a7f5c5f7ec41315f1c7238fd9ec0347684d6d99e086db81201ca21d1f5778

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
