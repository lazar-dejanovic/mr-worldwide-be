# Java build stage
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /app
COPY ./ .
RUN gradle --no-daemon bootJar

# Main stage
FROM amazoncorretto:21-alpine3.22-jdk
VOLUME /tmp

EXPOSE 8080
COPY --from=builder /app/build/libs/mr-worldwide.jar app.jar
ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-XX:+UseContainerSupport", "-Djava.awt.headless=true", "-jar", "app.jar"]

# docker build -f Dockerfile -t mr-worldwide .
# docker run -p 8080:8080 --name mr-worldwide mr-worldwide
