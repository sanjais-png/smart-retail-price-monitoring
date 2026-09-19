# Stage 1: Build Spring Boot Application with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY services services
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8090}"]
