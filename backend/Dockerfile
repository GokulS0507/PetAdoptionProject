# --------------------
# 1. BUILD STAGE
# --------------------
# Use a Maven image with JDK 17 (Java 17 is specified in your pom.xml) to build the application
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file to leverage Docker's build cache for dependencies
COPY pom.xml .
# Download dependencies. This skips re-downloading if pom.xml hasn't changed.
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, creating a "fat jar" in the target directory
# The project artifact name is 'pet-adoption-website-1.0.0.jar' based on your pom.xml.
RUN mvn clean package -DskipTests

# --------------------
# 2. RUN STAGE
# --------------------
# Use a smaller, secure JRE image for the final runtime environment
FROM eclipse-temurin:17-jre-focal

# Set a volume mount point for temporary files (good practice for Spring Boot)
VOLUME /tmp

# Copy the built JAR file from the 'build' stage
# Ensure this JAR name matches the artifact name and version from your pom.xml
COPY --from=build /app/target/pet-adoption-website-1.0.0.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Define the command to run the application (Render will use this as the start command)
ENTRYPOINT ["java", "-jar", "/app.jar"]