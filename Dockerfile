FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .

# Args para credenciales de GitHub Packages
ARG GPR_USER
ARG GPR_KEY

ENV GPR_USER=$GPR_USER
ENV GPR_KEY=$GPR_KEY

# Aseguramos que el wrapper sea ejecutable y construimos el jar ejecutable
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8081

COPY --from=build /workspace/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/spring-boot-application.jar"]
