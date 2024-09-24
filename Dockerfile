# Use an official Maven image for building the Spring Boot app
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Use a smaller runtime image: OpenJDK with Alpine Linux
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/ecom-0.0.1-SNAPSHOT.jar .

# Expose the default Spring Boot port
EXPOSE 8080

# Specify the command to run the application with memory settings
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "/app/ecom-0.0.1-SNAPSHOT.jar"]
