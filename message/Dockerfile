FROM openjdk:21-jdk-slim

LABEL "author" = "easycar"

# Install curl (and clean up to reduce image size)
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/message-0.0.1-SNAPSHOT.jar message-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "message-0.0.1-SNAPSHOT.jar"]