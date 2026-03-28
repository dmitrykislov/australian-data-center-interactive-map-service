# syntax=docker/dockerfile:1.7
# Multi-stage Dockerfile for DataCenter Mapping Application
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy build files first for better layer caching
COPY pom.xml .
COPY checkstyle.xml .

# Copy source code
COPY src ./src

# Build application with persistent Maven cache.
# Maven resolves local repo location from its own settings/defaults.
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy JAR from builder
COPY --from=builder /build/target/datacenter-mapping-*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f -k https://localhost:8443/api/datacenters || exit 1

# Expose HTTPS port
EXPOSE 8443

# Run application
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Dspring.profiles.active=production", "-jar", "app.jar"]