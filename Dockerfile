# Use an official Maven image as a build stage
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml into the container
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Ensure the Maven wrapper script has executable permissions
RUN chmod +x mvnw

# Download dependencies (this will cache dependencies if the pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the project files
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Use a lightweight JDK image for the runtime
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar tipster-admin.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "tipster-admin.jar"]