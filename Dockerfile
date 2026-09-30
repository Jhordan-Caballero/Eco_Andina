# Backend (Spring Boot, Java 21). El frontend tiene su propio Dockerfile en frontend/.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo el pom para reutilizar la capa de dependencias entre despliegues
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home --uid 10001 ecoandina
COPY --from=build /app/target/*.jar app.jar
USER ecoandina

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
