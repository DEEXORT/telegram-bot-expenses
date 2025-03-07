# Use OpenJDK 23 as the base image
FROM openjdk:23-slim AS build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package && ls -l /app/target

# Set the working directory for the runtime
WORKDIR /app

# Expose any necessary ports
EXPOSE 8080

# Run the built JAR
CMD ["java", "-jar", "target/telegram-bot-expenses-1.0-SNAPSHOT.jar"]