# ---- Stage 1: Build the application ----
FROM maven:3.9.9-amazoncorretto-17 AS build

# Copy source code and pom.xml file to /app folder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
#-DskipTests: không chạy unit test (cho nhanh, thường dùng ở production build).
RUN mvn package -DskipTests

# ---- Stage 2: Run the application ----
# Start with Amazon Corretto JDK 17
FROM amazoncorretto:17.0.15-alpine

# Set working folder to App and copy compiled file from above step
WORKDIR /app
# copy file jar ở stage 1 và đổi tên thành app.jar sau đó bỏ vào /app
COPY --from=build /app/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
