# ==========================================
# STAGE 1: Build the application using Maven
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy the Maven wrapper and project configuration files first (optimizes layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Give execution permissions to the Maven wrapper script
RUN chmod +x mvnw

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline

# Copy the actual source code
COPY src/ src/

# Build the fat executable JAR file (skip tests to speed up container build)
RUN ./mvnw clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy ONLY the compiled JAR file from Stage 1 into this final, lightweight runtime image
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 for web traffic
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]