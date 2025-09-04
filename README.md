# java-microservice

- http://localhost:8081/h2-console
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8081/actuator/busrefresh – Refreshes config properties provided by `configserver`. Works with `rabbitmq:3.13-management`.
- http://localhost:8081/actuator/shutdown – Deregisters the service from the Eureka server.
- http://localhost:8070/ – Eureka server
- http://localhost:7080/ – Keycloak

---

## Older Versions

- 2.0.0: Eureka Server is replaced by Kubernetes server-side service discovery, which is not supported in Docker Compose. Tiltfile is introduced for local development, but it has issues while pulling images from Tilt and pushing them into a Kubernetes cluster running locally. The file is supposed to be fixed in the next version.
- 1.0.0: V1 uses Eureka Server for client-side service discovery and can run with Docker Compose. The app can be ran by Docker Compose and Kubernetes (with or without Helm).

---

## Prerequisites

1. Local Kubernetes cluster – e.g. Minikube, Kind, or Docker Desktop Kubernetes
2. kubectl configured to talk to that cluster
3. Helm installed
4. Tilt installed
5. Java v21 installed
6. Maven installed

---

## How to run the project for local development

1. `cd <root-of-app>`
2. Make sure keycloak helm release is running (Run `helm list` to check it). If not, run `helm install keycloak helm/keycloak`
3. `make install-infra-helm`
4. `tilt up`

---

## How to stop the project for local development

1. `cd <root-of-app>`
2. `tilt down`
3. `make uninstall-infra-helm`

NOTE: If you uninstall keycloak's Helm release, you may need to delete pvc of the k8s pod too. Otherwise, when you re-install the release, keycloak may not work properly. However, by uninstalling keycloak's Helm release, you have to create clients, roles, and roles again (See `Set Up Clients, Roles, and Users` below for how to create them). If you're fine, run `helm uninstall keycloak`.

---

## Future Enhancements

- Introduce e2e testing.
- Remove RabbitMQ, Keycloak and Redis Helm chart folder from `/helm` folder. Those folders bloating this repo. Instead, try using charts by remote chart. For example:
```bash
# I haven't tested this approach
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install keycloak bitnami/keycloak -f my-values.yaml
```
- Set credentials for Redis. Currently, no authentication is required.
- Update every occurrence of `SPRING_RABBITMQ_HOST: "rabbit"` with `SPRING_RABBITMQ_HOST: "rabbitmq"` in Docker Compose and Kubernetes manifest files and update the service name from  `rabbit` to `rabbitmq`.
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

1. Start Keycloak by `helm install keycloak <directory-of-keycloak>`
2. Check if `keycloak` Kubernetes service is running by `kubectl get svc`
3. If `keycloak` is running, do port-forwarding (e.g., If the ports are `80:30510/TCP`, run `kubectl port-forward svc/keycloak 7080:80` where 7080 is any available port)
4. Open the admin console (e.g., `http://localhost:7080/admin/master/console/`)
5. Login: `admin` / `admin`

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

1. Get a client secret from Keycloak admin console (see above)  
2. Check if `gatewayserver` Kubernetes service is running by `kubectl get svc` and find the ports
3. Do port-forwarding (e.g., Given the exposed port is 8072, run `kubectl port-forward svc/gatewayserver 8072:8072`)
4. Open Postman and select a protected endpoint
5. Go to the **Authorization** tab
6. Set:
    - **Auth Type**: `OAuth 2.0`
    - **Add authorization data to**: `Request Headers`
    - **Token**: `authcode_accesstoken`
    - **Use Token Type**: `Access Token` (Token can be empty at the beginning)
    - **Header Prefix**: `Bearer`
    - **Token Name**: `authcode_accesstoken`
    - **Grant Type**: `Authorization Code`
    - **Authorize using browser**: Enabled
    - **Callback URL**: Use the default or your own
    - **Auth URL**: `http://localhost:7080/realms/master/protocol/openid-connect/auth` (Given that Keycloak service is running at localhost:7080) 
    - **Access Token URL**: `http://localhost:7080/realms/master/protocol/openid-connect/token` (Given that Keycloak service is running at localhost:7080)
    - **Client ID**: `easycar-client-authorization-code`
    - **Client Secret**: Get it from Keycloak UI (Clients → `easycar-client-authorization-code` → Credentials)
    - **Scope**: `openid email profile`
    - **State**: Any random string (e.g., `123456`)
    - **Client Authentication**: `Send client credentials in body`
4. Sign out of Keycloak UI if you're logged in
5. Click **Get New Access Token** in Postman

---

## Using Flyway for DB Migration

### Run Locally (local development)

```bash
mvn flyway:migrate   -Dflyway.url=jdbc:postgresql://localhost:<port>/<service>-service-db   -Dflyway.user=postgres   -Dflyway.password=secret
```

### Run Locally (Helm and Kubernetes)

```bash
# NOTE: This doesn't work on Powershell
kubectl port-forward svc/<service>-service-db 5432:5432
# Open a new terminal
cd <service>
mvn flyway:migrate   -Dflyway.url=jdbc:postgresql://localhost:5432/<service>-service-db   -Dflyway.user=postgres   -Dflyway.password=secret

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
### Set up Kubernetes service discovery (1 time activity)

```bash
kubectl apply -f kubernetes/kubernetes-discoveryserver.yml
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
# Crate a blank chart (e.g., easycar-common)
helm create <chart>

# Create a single manifest file that contains all the charts' information (This works only if there is no compilation issue, so use this for debugging)
helm template easycar helm/environments/dev/ > rendered.yaml

# Show all the charts' information
helm template easycar helm/environments/dev/

# Deploy easycar microservies for dev env (easycar is the release name)
helm install easycar helm/environments/dev/

# Shut down the deployments and services for easycar microservies on dev env
helm uninstall easycar

# Show all the releases
helm list

# Use this when a dependency for a chart is updated (e.g., If I update /helm/easycar-services/gatewayserver/values.yaml, I should cd to /helm/environments/dev and run helm dependencies build)
helm dependencies build

# <env> is dev, qa or prod
helm upgrade easycar <env>

# Show all the previous revisions
helm history easycar

# Rollback to a previous revision (<revisionNumber> is a number)
helm rollback easycar <revisionNumber>
```

### Miscellaneous

- Stop Helm releases when they aren't used, since they keep the PC busy.
- If I uninstall and install Keycloak chart, Keycloak's pod may crash loop backoff. In that case, remove pvc of Keycloak and trying installing again.

## Note

> Why is `@AllArgsConstructor` needed when using `@NoArgsConstructor` and `@Builder`?  
> `@Builder` generates an all-args constructor only if none exists. If you’ve defined `@NoArgsConstructor`, you must also define `@AllArgsConstructor`; otherwise, a compilation error like “actual and formal argument lists differ in length” may occur.
