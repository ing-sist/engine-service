FROM gradle:8.5.0-jdk21 AS build

WORKDIR /home/gradle/src
COPY . .

# Args para pasar user/token al build (GitHub Packages)
ARG GPR_USER
ARG GPR_KEY

# Usamos los args como propiedades que tu build.gradle ya sabe leer
RUN gradle --version

RUN gradle --no-daemon --stacktrace --info assemble \
    -Pgpr.user=$GPR_USER \
    -Pgpr.key=$GPR_KEY

FROM eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8081

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/spring-boot-application.jar"]
