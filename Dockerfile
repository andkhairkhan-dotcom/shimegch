# Multi-stage build for optimized production image
FROM maven:3.9.6-openjdk-21-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Pproduction

# Production stage
FROM openjdk:21-jdk-slim

# Install fonts for PDF generation
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/shimegch-1.0-SNAPSHOT.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
