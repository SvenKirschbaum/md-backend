FROM maven:3.9.9-amazoncorretto-21@sha256:ac4a9980f3fd8805e0bab5553eb17a2317a33003deb434839bc1d550669fb683 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.4-alpine@sha256:6a98c4402708fe8d16e946b4b5bac396379ec5104c1661e2a27b2b45cf9e2d16

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
