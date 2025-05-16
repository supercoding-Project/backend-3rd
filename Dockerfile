# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

ENV LANG='en_US.UTF-8' \
    LANGUAGE='en_US:en' \
    LC_ALL='en_US.UTF-8' \
    DB_USERNAME=root \
    DB_PASSWORD=1234 \
    REDIS_HOST=redis \
    REDIS_PORT=6379

WORKDIR /app

# jar 파일 복사
COPY build/libs/scheduler-0.0.1-SNAPSHOT.jar app.jar

# application.yml, .env 복사 (필요시)
COPY src/main/resources/application.yml /app/application.yml
COPY .env /app/.env

EXPOSE 8080 9092 9093

ENTRYPOINT ["java", "-jar", "app.jar"]
