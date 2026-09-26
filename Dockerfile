FROM maven:3.9.16-amazoncorretto-25@sha256:490bf1b0b852f8ae833f134933f30ca38024e4db475b2db05ee58b2f819179f0 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.4-alpine@sha256:4955796538972099d9c7de6e31c6a259b1de65393a58b7e0996b7cc50d7d20a7

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
