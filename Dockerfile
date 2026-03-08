# Estágio 1: Build
FROM ubuntu:latest AS build

RUN apt-get update && apt-get install openjdk-21-jdk maven -y
COPY . .
RUN mvn clean install


FROM eclipse-temurin:21-jre-alpine

EXPOSE 8080

COPY --from=build /target/todolist-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]