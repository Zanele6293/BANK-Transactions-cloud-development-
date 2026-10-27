FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src/ src/

# Build the fat executable JAR file
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy ONLY the compiled JAR file from Stage 1 into this final runtime image
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 for web traffic
EXPOSE 8080

# Run the compiled Spring Boot JAR file (NOT maven!)
ENTRYPOINT ["java", "-jar", "app.jar"]