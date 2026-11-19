# ---------------------------------------------------------------------------
# Image du micro-service account-service (build multi-etapes)
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY account-service/pom.xml account-service/pom.xml
RUN mvn -B -pl account-service -am dependency:go-offline
COPY account-service/src account-service/src
RUN mvn -B -pl account-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/account-service/target/account-service-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
