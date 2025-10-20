FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /ws

COPY pom.xml /ws/pom.xml
RUN mvn -q -N -f /ws/pom.xml install
COPY api-gateway/pom.xml /ws/api-gateway/pom.xml
RUN mvn -q -f /ws/api-gateway/pom.xml -DskipTests dependency:go-offline
COPY api-gateway /ws/api-gateway
RUN mvn -q -f /ws/api-gateway/pom.xml -DskipTests package

# ---------- RUNTIME ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /ws/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
