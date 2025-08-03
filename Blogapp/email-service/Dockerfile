# Multi-stage build for email service
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy the email service source code
COPY . .

# Build the email service using the main pom.xml
RUN mvn clean package -f pom.xml -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/email-service-*.jar email-service.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "email-service.jar"] 