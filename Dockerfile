# ==========================
# 1. Build Stage
# ==========================
FROM docker.io/maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only the pom.xml first (to leverage Docker cache for dependencies)
COPY pom.xml .

# Download dependencies (without building)
RUN mvn dependency:go-offline -B

# Copy the actual source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# ==========================
# 2. Runtime Stage
# ==========================
FROM docker.io/eclipse-temurin:21-jre-alpine AS runtime

# Add a non-root user for security
RUN addgroup -S apps && adduser -S walleapps -G apps

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Add JVM options as an environment variable
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Expose the application port
EXPOSE 8080

# Add a health check (optional, depends on your app endpoint)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run as non-root user
USER walleapps

# Run the app with JVM tuning
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]