# java-microservice

- http://localhost:8081/h2-console
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8081/actuator/busrefresh (It refreshes config properties set by `configserver`. It works on `rabbitmq:3.13-management`.)
- http://localhost:8081/actuator/shutdown (De-register from Eureka server)
- http://localhost:8070/ (Eureka server)

## Future enhancements
- Currently, `easycar-correlation-id` is used for logging inter-service communications (See `com.easycar.gatewayserver.filters` package of gatewayserver). Consider introducing Micrometer for global logging.
- `gatewayserver` implements a circuit breaker to `order-service`. Try implementing other resiliency patterns - [rate limit](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945186) and [retry](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945166) patterns. Before doing so, consider which pattern should be used for which situation.
- Create a util class to clean up the logic for getting `"realm_access"` from JWT. e.g.:
```
public class JwtUtil {
    public static List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();
        return (List<String>) realmAccess.getOrDefault("roles", List.of());
    }
}
```
- Introduce Apache Kafka. Find a use case where Kafka is more suitable than RabbitMQ and incorporate it. See [this page](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945898#overview) to use Kafka with Docker. Note that using both Kafka and RabbitMQ with Spring Cloud Stream is probably impossible. Use Kafka with Spring Boot without Spring Cloud.


## Dependencies

- RabbitMQ (Use `docker run -d -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management`)
- Keycloak (Use `docker run -d -p 7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.5 start-dev`)

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

- docker build . -t <dockerhub-user-name>-<image-name>:<tag>
- docker images
- docker run -d -p <port-of-local-machine>:<port-used-by-the-image> <image-id> (e.g. `docker run -d -p 8080:8081 22b19` -> The app should be available at `http://localhost:8080/` in the local machine)
- docker start <container-id> (It runs an existing container, not an image)
- docker stop <container-id> (It stops an existing container, not an image)
- docker ps -a
- docker image push docker.io/<dockerhub-user-name>/<image-name>:<tag>
- docker compose up -d --build (Use it under `/docker-compose/<env>` folder)
- docker compose -f docker-compose/<env>/docker-compose.yml up -d --build (Use it under the root of the project)
- docker compose down (It deletes containers)
- docker compose stop (It doesn't delete containers)
- docker compose start (It runs existing containers)

## How to use the Makefile
- make # builds all the services and make images of them
- make build-jar
- make build-images

## How to run infra services via Docker
- docker run --name product-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=product-service-db -v postgres-data:/var/lib/postgresql/data -p 5432:5432 -d postgres:17.4 (This will persist the DB files even if the container is removed)
- docker run --name product-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=product-service-db -p 5432:5432 -d postgres:17.4
- docker run --name order-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=order-service-db -p 5433:5432 -d postgres:17.4
- docker run -it --rm --name easycar-rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3.13-management

## How to run all the services via IDE
1. Start RabbitMQ (Use `docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management` or restart an existing Docker container)
2. Start DBs of product and order services 
3. Start configserver
3. Start eurekaserver
4. Start product and order services
5. Start gatewayserver

## How to format code
- mvn spotless:apply

## Keycloak

### Set up a client, roles, and users
`gatewayserver` protects API request by [authentication code grant type flow](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945514#overview). To use the protected endpoints, set up a client, ream roles and users.
Run the Keycloak instance and do the following:
- Click Clients -> Create client. Set Client ID (`easycar-client-authorization-code`). Enable Client authentication, disable Authorization, check only Standard flow. Put * to Valid redirect URIs and Web origins.
- Click Realm roles -> Create role. Put INTERNAL_USER to Role name and save it. Do the same by putting CUSTOMER as the Role name. 
- Click Users -> Add user. Create two users, one for a user and the other for an internal user, while Email verified is enabled. When a user is created, click Credentials tab on the user's detail page, and add password. Then, click Role mapping tab, click Assign role. Click the filter icon and choose Filter by realm roles and choose CUSTOMER or INTERNAL_USER.

### Use tokens for API endpoints

- Sign out from Keycloak
- Open Postman, choose one of the endpoints that requires a token. Click Authorization tab, scroll down and find Get New Access Token button.
- Click the button and you should see a login page of Keycloak

### How to use Flyway for DB migration

- Run this command for migrating a DB locally (replace **** with the port and the service name):

```
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:****/****-service-db \
  -Dflyway.user=postgres \
  -Dflyway.password=secret
```
(Future enhance idea) In CI/CD, set url/user/password to env variables and try DB migration by something like:

```
mvn flyway:migrate \
  -Dflyway.url=$FLYWAY_URL \
  -Dflyway.user=$FLYWAY_USER \
  -Dflyway.password=$FLYWAY_PASSWORD
```

- Run `./flyway-create.sh` at the root of the service (e.g. product-service) to generate a new migration file
