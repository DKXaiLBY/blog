# Stage 1: Build Spring Boot JAR with Maven Wrapper
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

# Stage 2: Run with minimal JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/blog-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=mysql"]
