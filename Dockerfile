# Stage 1: Build the Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Build the application, skipping tests for faster build
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy static frontend files (optional, if not included in JAR)
COPY src/main/resources/static/index.html ./static/index.html
COPY src/main/resources/static/style.css ./static/style.css

# Expose the default Spring Boot port
EXPOSE 8080

# Set environment variables (can be overridden)
ENV SPRING_DATA_MONGODB_URI=mongodb+srv://root:Girish%40123@java.k7m6yw4.mongodb.net/sample_mflix?retryWrites=true&w=majority&appName=JAVA
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
