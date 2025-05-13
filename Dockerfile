# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set environment variables (DB, Redis, etc.)
ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    LC_ALL='en_US.UTF-8' \
    DB_USERNAME=root \
    DB_PASSWORD=1234 \
    REDIS_HOST=redis-container \
    REDIS_PORT=6379

# Set working directory
WORKDIR /app

# Copy build files (Assuming a jar built with Gradle or Maven)
COPY build/libs/scheduler-0.0.1-SNAPSHOT.jar app.jar

# Copy configuration files (application.yml)
COPY src/main/resources/application.yml /app/application.yml

# .env 파일 복사
COPY .env /app/.env

# Expose necessary ports
EXPOSE 8080
EXPOSE 9092
EXPOSE 9093


# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]

