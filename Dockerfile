FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package \
 && ls -la target \
 && cp target/*.jar /build/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
