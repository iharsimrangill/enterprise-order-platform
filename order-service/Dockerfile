# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl order-service -am dependency:go-offline -B

COPY . .

RUN mvn -pl order-service -am clean package -DskipTests -B

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --uid 10001 spring

COPY --from=builder /workspace/order-service/target/*.jar app.jar

USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
