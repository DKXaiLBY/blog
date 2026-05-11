# 后端 Spring Boot Dockerfile
# 构建：mvnw package -DskipTests（在 blog-server 目录）
# 镜像：docker build -t blog-server .

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/blog-server-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=mysql"]
