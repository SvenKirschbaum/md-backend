FROM maven:3.9.14-amazoncorretto-25@sha256:8ef71316571ea4d119086e49277cd20330e8c2600e94601613df86703df3e82f as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.2-alpine@sha256:29fb372e1090a314688d0105870559369f8481ad18826e1c2367eab67e6eca4b

WORKDIR /usr/locale/markusdope-backend

COPY --from=build /build/target/MarkusDopeStats.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
