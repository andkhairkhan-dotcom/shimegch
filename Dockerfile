# Use Eclipse Temurin for better compatibility
FROM eclipse-temurin:21-jdk

# Install Maven
RUN apt-get update && apt-get install -y \
    maven \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Pproduction

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "target/shimegch-1.0-SNAPSHOT.jar"]
