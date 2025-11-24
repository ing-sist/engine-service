FROM gradle:8.5.0-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src

ARG GPR_USER
ARG GPR_KEY

#Uso los argumentos solo para este comando especifico y no exponer los env
RUN gradle assemble \
    -Pgpr.user=$GPR_USER \
    -Pgpr.key=$GPR_KEY \

FROM eclipse-temurin:21-jre

EXPOSE 8081

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production","/app/spring-boot-application.jar"]