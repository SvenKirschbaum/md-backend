FROM maven:3.9.8-amazoncorretto-21@sha256:e2350237ba55cd92d9e0ec649b1e3b261c862a2c1459dd272fa4ff9490c17612 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.3-alpine@sha256:0b1766ef32ffa1fbfeb364d31072175d995c881c664e334cd687eeaf216c2ffe

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
