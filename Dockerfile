FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .

# Args para credenciales de GitHub Packages
ARG USERNAME
ARG TOKEN

ENV USERNAME=$USERNAME
ENV TOKEN=$TOKEN

# Aseguramos que el wrapper sea ejecutable y construimos el jar ejecutable
RUN chmod +x ./gradlew \
    && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8081

COPY --from=build /workspace/build/libs/*.jar /app/spring-boot-application.jar

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

FROM eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8081

COPY --from=build /workspace/build/libs/*.jar /app/spring-boot-application.jar

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-Dspring.profiles.active=production", "-jar", "/app/spring-boot-application.jar"]
