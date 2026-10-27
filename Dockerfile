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