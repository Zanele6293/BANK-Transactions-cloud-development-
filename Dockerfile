# Use a lightweight official Java runtime image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/*.jar app.jar

# Expose port 8080 so the cloud network can reach your web app
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]