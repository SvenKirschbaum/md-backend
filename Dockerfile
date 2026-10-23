FROM maven:3.10.0-amazoncorretto-25@sha256:d96c6ff2e7a3258e6f0c447340d2284a3404917bb8c5f01c8660cc4066bd382c as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.5-alpine@sha256:b611ab31a8cff0ab60ff5ee26de46d06922bfff0c54f5029ac588d5337bc533b

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
