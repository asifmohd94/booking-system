# ============================
# Stage 1 - Build the application
# ============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven wrapper and configuration
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Download dependencies first (improves Docker layer caching)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests

# ============================
# Stage 2 - Runtime
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Application port
EXPOSE 8080

# JVM Options (optional)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]