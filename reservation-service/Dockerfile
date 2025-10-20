FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /ws

COPY pom.xml /ws/pom.xml
RUN mvn -q -N -f /ws/pom.xml install
COPY common/pom.xml /ws/common/pom.xml
RUN mvn -q -f /ws/common/pom.xml -DskipTests dependency:go-offline
COPY common /ws/common
RUN mvn -q -f /ws/common/pom.xml -DskipTests install
COPY reservation-service/pom.xml /ws/reservation-service/pom.xml
RUN mvn -q -f /ws/reservation-service/pom.xml -DskipTests dependency:go-offline
COPY reservation-service /ws/reservation-service
RUN mvn -q -f /ws/reservation-service/pom.xml -DskipTests package

# ---------- RUNTIME ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /ws/reservation-service/target/*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
