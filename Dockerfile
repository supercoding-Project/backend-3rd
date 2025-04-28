# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set environment variables
ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    LC_ALL='en_US.UTF-8'

# Set working directory
WORKDIR /app

# Copy build files (Assuming a jar built with Gradle or Maven)
COPY build/libs/scheduler-0.0.1-SNAPSHOT.jar app.jar

# Expose necessary ports
EXPOSE 8080
EXPOSE 9092
EXPOSE 9093

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=local"]

