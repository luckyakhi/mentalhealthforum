# Stage 1: The Build Stage (Now using JDK 21)
# We update the base image tag to '...-temurin-21'.
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: The Runtime Stage (Now using JDK 21)
# We update the base image tag to '21-jdk-slim'.
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]