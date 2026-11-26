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
# curl: lo usa el healthcheck de docker compose y scripts/salud.sh para consultar /actuator/health
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home --uid 10001 ecoandina \
    && mkdir -p /app/logs /app/uploads \
    && chown ecoandina /app/logs /app/uploads
COPY --from=build /app/target/*.jar app.jar
USER ecoandina

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
