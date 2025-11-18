FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 👇 add this before using curl
RUN apk add --no-cache curl
# Create a non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser
# Copy the built JAR from the build stage
COPY --from=build /app/target/clinic-appointment-system-*.jar app.jar
# Copy data.sql to the application's data directory
COPY src/main/resources/data.sql /app/data/data.sql
# Create data directory and set ownership for the non-root user
RUN mkdir -p /app/data && \
    chown -R appuser:appuser /app
# Switch to the non-root user
USER appuser
# Expose the port the application runs on (changed to 8090 to avoid Jenkins conflict)
EXPOSE 8090
# Define the healthcheck endpoint (updated to match the new port)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]