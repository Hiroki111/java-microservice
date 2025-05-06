FROM openjdk:21-jdk-slim

LABEL "author" = "easycar"

WORKDIR /app

COPY target/order-service-0.0.1-SNAPSHOT.jar order-service-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "order-service-0.0.1-SNAPSHOT.jar"]