FROM openjdk:21-jdk-slim

LABEL "author" = "easycar"

COPY target/product-service-0.0.1-SNAPSHOT.jar product-service-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "product-service-0.0.1-SNAPSHOT.jar"]