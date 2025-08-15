# java-microservice

- http://localhost:8081/h2-console
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8081/actuator/busrefresh – Refreshes config properties provided by `configserver`. Works with `rabbitmq:3.13-management`.
- http://localhost:8081/actuator/shutdown – Deregisters the service from the Eureka server.
- http://localhost:8070/ – Eureka server
- http://localhost:7080/ – Keycloak

---

## Future Enhancements

- Start using `bitnami/rabbitmq:4.1.3-debian-12-r1` for Docker Compose. This is a stable Docker image used by Bitnami's Docker Helm chart that I use. Now, rabbitmq's v3.13.7 is used for Docker Compose, but I don't see Bitnami RabbitMQ chart that uses this version.
- Update every occurance of `SPRING_RABBITMQ_HOST: "rabbit"` with `SPRING_RABBITMQ_HOST: "rabbitmq"` in Docker Compose and Kubernetes manifest files and update the service name from  `rabbit` to `rabbitmq`.
- Currently, `easycar-correlation-id` is used for logging inter-service communication (see the `com.easycar.gatewayserver.filters` package in `gatewayserver`). Consider using Micrometer for centralized logging.
- `gatewayserver` implements a circuit breaker for `order-service`. Try implementing additional resiliency patterns such as [rate limiting](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945186) and [retry](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945166). Consider which pattern is best suited for each scenario before implementing them.
- Create a utility class to clean up the logic for extracting `"realm_access"` roles from JWTs. For example:

```java
public class JwtUtil {
    public static List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();
        return (List<String>) realmAccess.getOrDefault("roles", List.of());
    }
}
```

- Introduce Apache Kafka. Identify a use case where Kafka is more appropriate than RabbitMQ and integrate it. See [this page](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945898#overview) for using Kafka with Docker. Note that using both Kafka and RabbitMQ with Spring Cloud Stream is likely not supported. Use Kafka with Spring Boot directly (without Spring Cloud).
- The `GET /api/product` endpoint caches results but does not handle race conditions. Consider strategies to prevent this issue.

---

## Running a Java Microservice from a .jar File

1. Ensure your `pom.xml` includes `<packaging>jar</packaging>` at the same level as `<artifactId>`.
2. In a terminal, `cd` to the root of the microservice.
3. Run `mvn clean install`.
4. A `.jar` file will be generated in the `target` folder. This file includes all dependencies (e.g., Spring libraries, embedded Tomcat) except the Java runtime.
5. Start the service with `mvn spring-boot:run` or `java -jar target/<jar-file-name>.jar`.

---

## Running a Service with Docker

1. Build the `.jar` file (as described above).
2. Create a `Dockerfile`.
3. Use `docker build` and `docker run` (see commands below).
4. To use Docker Compose, add service definitions to `docker-compose.yml`.

(To run all the services together, `cd` to `/docker-compose/default` and run `docker compose up`)
---

## Docker Command Cheat Sheet

```bash
docker build . -t <dockerhub-username>/<image-name>:<tag>
docker images
docker run -d -p <host-port>:<container-port> <image-id>  # Example: docker run -d -p 8080:8081 22b19
docker start <container-id>  # Start an existing container
docker stop <container-id>   # Stop an existing container
docker ps -a
docker image push docker.io/<dockerhub-username>/<image-name>:<tag>
docker compose up -d --build  # Run from `/docker-compose/<env>` folder
docker compose up -d --build --force-recreate  # Recreate containers (useful if configserver DNS is outdated)
docker compose -f docker-compose/<env>/docker-compose.yml up -d --build
docker compose stop     # Stop containers without deleting
docker compose start    # Start stopped containers
docker compose down     # DON'T USE THIS because it removes containers, so DB and Keycloak data will be erased
```

---

## Using the Makefile

```bash
make                  # Build all services and create images
make build-jar
make build-images
```

---

## Running Infrastructure Services with Docker

```bash
# Product service DB (persistent)
docker run --name product-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=product-service-db -v postgres-data:/var/lib/postgresql/data -p 5432:5432 -d postgres:17.4

# Product service DB (non-persistent)
docker run --name product-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=product-service-db -p 5432:5432 -d postgres:17.4

# Order service DB
docker run --name order-service-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=order-service-db -p 5433:5432 -d postgres:17.4

# RabbitMQ
docker run -d -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management

# Redis
docker run --name redis -d -p 6379:6379 redis

# Keycloak  
docker run -d -p 7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.5 start-dev
```

---

## Running All Services via IDE

1. Start RabbitMQ  
   `docker run -d -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management`
2. Start Redis
   `docker run --name redis -d -p 6379:6379 redis`
3. Start Keycloak
   `docker run -d -p 7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.5 start-dev`
4. Start the databases for product and order services
3. Start `configserver`
4. Start `eurekaserver`
5. Start `product` and `order` services
6. Start `gatewayserver`

---

## Code Formatting

```bash
mvn spotless:apply
```

---

## Keycloak

### Set Up Clients, Roles, and Users

`gatewayserver` protects API requests using the [Authorization Code Grant](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945514#overview) flow.

To access protected endpoints:

1. Start Keycloak (`http://localhost:7080/`) and open the admin console (`http://localhost:7080/admin/master/console/`)
2. Login: `admin` / `admin`

Then:

1. **Create a Client**
    - Navigate to **Clients** → **Create client**
    - Type: OpenID Connect
    - Client ID: `easycar-client-authorization-code`
    - Enable *Client authentication*, disable *Authorization*, check only *Standard flow*
    - Set `*` as the value for Valid Redirect URIs and Web Origins (use specific URIs in production)
    - Save

2. **Create Users**
    - Navigate to **Users** → **Add user**
    - Enable *Email verified*, set a username (e.g., `customer`, `internaluser`), create
    - Go to **Credentials** tab → set password → disable *Temporary* → Save

3. **Create Roles**
    - Navigate to **Realm roles** → **Create role**
    - Create roles named `INTERNAL_USER` and `CUSTOMER` (These roles are used in `gatewayserver` code)

4. **Assign Roles to Users**
    - Navigate to **Users** → Choose the user created by Step 2 → **Role Mappings** → **Assign Role** → **Realm roles**
    - Select a role → Assign

---

### Using Protected Endpoints with Postman

1. Open Postman and select a protected endpoint
2. Go to the **Authorization** tab
3. Set:
    - **Auth Type**: `OAuth 2.0`
    - **Add authorization data to**: `Request Headers`
    - **Token**: `authcode_accesstoken`
    - **Use Token Type**: `Access Token` (Token can be empty at the beginning)
    - **Header Prefix**: `Bearer`
    - **Token Name**: `authcode_accesstoken`
    - **Grant Type**: `Authorization Code`
    - **Authorize using browser**: Enabled
    - **Callback URL**: Use the default or your own
    - **Auth URL**: `http://localhost:7080/realms/master/protocol/openid-connect/auth`
    - **Access Token URL**: `http://localhost:7080/realms/master/protocol/openid-connect/token`
    - **Client ID**: `easycar-client-authorization-code`
    - **Client Secret**: Get it from Keycloak UI (Clients → `easycar-client-authorization-code` → Credentials)
    - **Scope**: `openid email profile`
    - **State**: Any random string (e.g., `123456`)
    - **Client Authentication**: `Send client credentials in body`
4. Sign out of Keycloak UI if you're logged in
5. Click **Get New Access Token** in Postman

---

## Using Flyway for DB Migration

### Run Locally

```bash
mvn flyway:migrate   -Dflyway.url=jdbc:postgresql://localhost:<port>/<service>-service-db   -Dflyway.user=postgres   -Dflyway.password=secret
```

### In CI/CD (Idea)

```bash
mvn flyway:migrate   -Dflyway.url=$FLYWAY_URL   -Dflyway.user=$FLYWAY_USER   -Dflyway.password=$FLYWAY_PASSWORD
```

### Create New Migration File

Run `./flyway-create.sh` from the root of a service (e.g., `product-service`).

---

## Kubernetes

### Where to set shortcuts

On Windows, run `notepad $PROFILE`.
On Linux or Git Bash, run `nano ~/.bashrc`.


### Installing and running K8s dashboaard UI

1. Install Helm: https://helm.sh/docs/intro/install/
2. Follow this guide: https://kubernetes.io/docs/tasks/access-application-cluster/web-ui-dashboard/#deploying-the-dashboard-ui

Commands:

```bash
# Run this to install kubernetes-dashboard
helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --create-namespace --namespace kubernetes-dashboard

# Then run this
kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443

# If the above command causes a CrashLoopBackOff error in kong, you would need to re-install kubernetes-dashboard with --set kong.admin.tls.enabled=false
helm uninstall kubernetes-dashboard -n kubernetes-dashboard
helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --namespace kubernetes-dashboard --set kong.proxy.image.tag=3.6 --set kong.admin.tls.enabled=false

# Then try this again
kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443

# Use this to generate a token
kubectl -n kubernetes-dashboard create token admin-user
```

### Running All Services via Kubernetes

1. Run `cd` to `kubernetes`
2. Run `k apply -f rabbitmq.yml`, `k apply -f configserver.yml`


### Useful commands

```bash
# Apply a deployment manifest file (Use it at the root folder)
kubectl apply -f kubernetes/<manifest-file>.yml

# Make configserver's pod accessible via localhost:8071
kubectl port-forward service/configserver 8071:8071

kubectl delete pod <pod-name>

# Change the size of <deployment-name> to <number>
kubectl scale deployment <deployment-name> --replicas=<number>
```

---

## Helm

### Useful commands

```bash
# Keycloak
# To access Keycloak from outside the cluster execute the following commands:
# 1. Get the Keycloak URL by running these commands:
export HTTP_SERVICE_PORT=$(kubectl get --namespace default -o jsonpath="{.spec.ports[?(@.name=='http')].port}" services keycloak)
export SERVICE_IP=$(kubectl get svc --namespace default keycloak -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# 2. Access Keycloak using the obtained URL.
echo "http://${SERVICE_IP}:${HTTP_SERVICE_PORT}/"

# 3. Access the Administration Console using the following credentials:
echo Username: admin
echo Password: $(kubectl get secret --namespace default keycloak -o jsonpath="{.data.admin-password}" | base64 -d)

#  NOTE: It may take a few minutes for the LoadBalancer IP to be available.
#        You can watch its status by running:
kubectl get --namespace default svc -w keycloak

# See which IP is used by running:
kubectl get svc keycloak

# If the external IP isn't localhost, do port-forwarding (change <port> with what you see by `kubectl get svc keycloak`):
kubectl port-forward svc/keycloak 8080:<port>
```

## Note

> Why is `@AllArgsConstructor` needed when using `@NoArgsConstructor` and `@Builder`?  
> `@Builder` generates an all-args constructor only if none exists. If you’ve defined `@NoArgsConstructor`, you must also define `@AllArgsConstructor`; otherwise, a compilation error like “actual and formal argument lists differ in length” may occur.
