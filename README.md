# java-microservice

- http://localhost:8081/h2-console
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8081/actuator/busrefresh (refresh config properties set by `configserver`)


## Dependencies

- RabbitMQ (Use `docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management`)

## How to run a Java microservice from a .jar file

1. Make sure that pom.xml has `<packaging>jar</packaging>` tag in the same level as `<artifactId>`
2. Open a terminal and `cd` to the root of the microservice 
3. Run `mvn clean install`
4. There will be a .jar file in `target` folder. The .jar file has all the dependencies (e.g. Spring libraries, Tomcat server) except the runtime. 
5. Run `mvn spring-boot:run` or `java -jar target/<jar-file-name>.jar`

## How to run a service by Docker

1. Build the .jar file by following (see above)
2. Create Dockerfile
3. Use `docker build` and `docker run` commands (see below)
4. To work with Docker compose, add the service information to docker-compose.yml 

## Docker command cheat sheet

- docker build . -t <dockerhub-user-name>/<image-name>:<tag>
- docker images
- docker run -d -p <port-of-local-machine>:<port-used-by-the-image> <image-id> (e.g. `docker run -d -p 8080:8081 22b19` -> The app should be available at `http://localhost:8080/` in the local machine)
- docker start <container-id> (It runs an existing container, not an image)
- docker stop <container-id> (It stops an existing container, not an image)
- docker ps -a
- docker image push docker.io/<dockerhub-user-name>/<image-name>:<tag>
- docker compose up -d (Use it under `/docker-compose/<env>` folder)
- docker compose -f docker-compose/<env>/docker-compose.yml up -d --build (Use it under the root of the project)
- docker compose down (It deletes containers)
- docker compose stop (It doesn't delete containers)
- docker compose start (It runs existing containers)

## How to use the Makefile
- make # builds all the services and make images of them
- make build-jar
- make build-images
