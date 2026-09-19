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
COPY --from=build /app/target/smart-retail-price-monitoring-1.0.0.jar app.jar
EXPOSE 8090
ENV PORT=8090
ENTRYPOINT ["java", "-jar", "app.jar"]
