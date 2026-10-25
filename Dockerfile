FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21.0.8_9-jre-noble

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 --create-home appuser

WORKDIR /app

COPY --from=build /workspace/target/backendforge-0.0.1-SNAPSHOT.jar /app/backendforge.jar

RUN chown appuser:appuser /app/backendforge.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=6 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/backendforge.jar"]
