FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon
EXPOSE 8080
CMD ["java", "-jar", "build/libs/product-api-0.0.1-SNAPSHOT.jar"]
