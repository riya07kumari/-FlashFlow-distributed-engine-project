# ==========================================
# Multi-stage Dockerfile for FlashFlow Backend
# ==========================================

# 1. Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace/app

# Cache dependencies
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy source and package
COPY src src
RUN ./mvnw clean package -DskipTests -B

# 2. Production Runtime Stage
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Run as non-privileged system user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact
COPY --from=builder /workspace/app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD wget -q -O - http://localhost:8080/v3/api-docs || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
