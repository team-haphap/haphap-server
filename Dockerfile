FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
COPY build/libs/*.jar app.jar
USER spring
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
