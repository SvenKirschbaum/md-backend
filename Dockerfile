FROM maven:3.9.6-amazoncorretto-21@sha256:f004b534b13f0a785ad0a904ed012606b0939b40344939fe7e4cf1be9a3f92e5 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.2-alpine@sha256:ecdb53d62a45cb978b849e30ebcc16933ad5c2a7659b94f0c556b93fe575cda9

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
