FROM maven:3.9.6-eclipse-temurin-21 AS buildstage 

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=buildstage /app/target/*.jar /app/app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "/app/app.jar"]