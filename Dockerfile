FROM maven:3.9.7-amazoncorretto-21@sha256:09e85c48daef769f40303b123f713829b65ce5473f838b2c1254cbb143eef151 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.3-alpine@sha256:e85c947a4f836433fdb0f2c117b50e0dbb11a2eef3811a249ef00b068f6bce9a

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
