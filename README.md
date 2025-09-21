# java-microservice

## Version History

- **2.1.0** – Introduced a common module in the Spring Boot BOM. Replaced external Bitnami Helm charts with remote ones that are found by `helm repo add bitnami https://charts.bitnami.com/bitnami`.
- **2.0.0** – Replaced Eureka Server with Kubernetes server-side service discovery (not supported in Docker Compose). Introduced Spring Boot BOM, jib, and Skaffold.
- **1.0.0** – Used Eureka Server for client-side service discovery. Can run with Docker Compose or Kubernetes (with or without Helm).

---

## Prerequisites

1. **Kubernetes cluster**
    - Skaffold and Minikube (only required if you want to run the app with Skaffold).
    - Other clusters such as Kind or Docker Desktop Kubernetes also work.
2. **kubectl** configured for your cluster
3. **Helm** installed
4. **Java 21** installed
5. **Maven** installed
6. *(Optional)* **Skaffold** installed ([quickstart](https://skaffold.dev/docs/quickstart/))

---

## Running the Project

1. Go to the project root:
   ```bash
   cd <root-of-app>
   ```
2. Start your Kubernetes cluster. If you use minikube, start it by `minikube start --cpus 4 --memory 4096`. [By default, minikube uses 2048MB](https://github.com/kubernetes/minikube/blob/232080ae0cbcf9cb9a388eb76cc11cf6884e19c0/pkg/minikube/constants/constants.go#L102), which isn't sufficient most of the time.
3. Check if the Keycloak Helm release is running:
   ```bash
   helm list
   ```
   If not, install it:
   ```bash
   helm install keycloak bitnami/keycloak --version 25.1.2  --values helm/external/keycloak/values.yaml
   ```
4. Install infrastructure:
   ```bash
   make install-infra-helm
   ```
5. Start the app:
   ```bash
   helm install easycar helm/environments/dev/
   ```
   or, if Skaffold is installed:
   ```bash
   skaffold dev
   ```

### Stopping the App

- If you started it with Helm:
  ```bash
  helm uninstall easycar
  make uninstall-infra-helm
  ```
- If you uninstall Keycloak, you may also need to delete its PVCs. Otherwise, reinstalling may fail.
  ```bash
  helm uninstall keycloak
  kubectl delete pvc -l app.kubernetes.io/instance=keycloak
  ```
  ⚠️ After reinstalling Keycloak, you must re-create clients, roles, and users. See [Set Up Clients, Roles, and Users](#set-up-clients-roles-and-users).

---

## Future Enhancements

- Currently, `easycar-correlation-id` is used for logging inter-service communication (see the `com.easycar.gatewayserver.filters` package in `gatewayserver`). Consider using Micrometer for centralized logging.
- `gatewayserver` implements a circuit breaker for `order-service`. Try implementing additional resiliency patterns such as [rate limiting](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945186) and [retry](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945166). Consider which pattern is best suited for each scenario before implementing them.
- Update every occurrence of `SPRING_RABBITMQ_HOST: "rabbit"` with `SPRING_RABBITMQ_HOST: "rabbitmq"` in Docker Compose and Kubernetes manifest files and update the service name from  `rabbit` to `rabbitmq`.
- Introduce e2e testing.
- Introduce Apache Kafka. Identify a use case where Kafka is more appropriate than RabbitMQ and integrate it. See [this page](https://www.udemy.com/course/master-microservices-with-spring-docker-kubernetes/learn/lecture/39945898#overview) for using Kafka with Docker. Note that using both Kafka and RabbitMQ with Spring Cloud Stream is likely not supported. Use Kafka with Spring Boot directly (without Spring Cloud).
- Set credentials for Redis. Currently, no authentication is required.
- The `GET /api/product` endpoint caches results but does not handle race conditions. Consider strategies to prevent this issue.
- `skaffold debug` doesn't break properly. Breakpoints aren't hit while I put them on IntelliJ. I may need to use Cloud Code as [recommended here](https://skaffold.dev/docs/workflows/debug/#recommended-debugging-using-cloud-code).

---

## Running a Java Microservice from a .jar File

1. Ensure your `pom.xml` includes `<packaging>jar</packaging>` at the same level as `<artifactId>`.
2. In a terminal, `cd` to the root of the microservice.
3. Run `mvn clean install`.
4. A `.jar` file will be generated in the `target` folder. This file includes all dependencies (e.g., Spring libraries, embedded Tomcat) except the Java runtime.
5. Start the service with `mvn spring-boot:run` or `java -jar target/<jar-file-name>.jar`.

---

## How to use Jib to containerize Java apps

```bash
# Builds to a container image registry.
mvn compile jib:build
# Builds to a Docker daemon.
mvn compile jib:dockerBuild
```

---

## How to refresh config properties provided by `configserver`

- Do port-forward for the service that needs to refresh its config properties (e.g., `kubectl port-forward service/product-service 8081:8081`)
- Hit `POST http://localhost:8081/actuator/busrefresh`

---

## Using the Makefile for build jar files of all the services

```bash
make                  # Build all services and create images
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

1. Start Keycloak by `helm install keycloak bitnami/keycloak --version 25.1.2  --values helm/external/keycloak/values.yaml`
2. Check if `keycloak` Kubernetes service is running by `kubectl get svc`
3. If `keycloak` is running, do port-forwarding (e.g., If the ports are `80:30510/TCP`, run `kubectl port-forward svc/keycloak 7080:80` where 7080 is any available port)
4. Open the admin console (e.g., `http://localhost:7080/admin/master/console/`)
5. Login: `admin` / `admin` (This credential is set in Keycloak helm chart under `/helm` folder)

Then:

1. **Create a Client**
    - Navigate to **Clients** → **Create client**
    - Type: OpenID Connect
    - Client ID: `easycar-client-authorization-code`
    - Click Next
    - Enable *Client authentication*, disable *Authorization*, check only *Standard flow*
    - Click Next
    - Set `*` as the value for *Valid Redirect URIs* and *Web Origins* (use specific URIs in production)
    - Save

2. **Create Users**
    - Navigate to **Users** → **Add user**
    - Enable *Email verified*, set a username (e.g., `customer`, `internaluser`), create
    - Go to **Credentials** tab → set password → enter password and disable *Temporary* → Save

3. **Create Roles**
    - Navigate to **Realm roles** → **Create role**
    - Create roles named `INTERNAL_USER` and `CUSTOMER` (These roles are used in `gatewayserver` code)

4. **Assign Roles to Users**
    - Navigate to **Users** → Choose the user created by Step 2 → **Role Mappings** → Arrow icon next to **Assign Role** → **Realm roles**
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

Use Flyway when the DB is empty without running the service that uses the DB. Otherwise, Flyway doesn't work due to missing `flywa_schema_history`.

### Run Locally (local development)

```bash
mvn flyway:migrate   -Dflyway.url=jdbc:postgresql://localhost:<port>/<service>-service-db   -Dflyway.user=postgres   -Dflyway.password=secret
```

### Run Locally (Helm and Kubernetes)

```bash
kubectl port-forward svc/<service>-service-db 5432:5432
# Open a new terminal (Don't use Powershell, since PowerShell treats : and = inside arguments differently and often messes with -D system property flags)
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
- If I uninstall and install Keycloak chart, Keycloak's pod may crash loop backoff. In that case, remove pvc of Keycloak (See below how to do it) and trying installing again.
- Use `kubectl delete pvc <pvc-name>` to delete one PVC by its exact name. 
- Use `kubectl delete pvc -l app.kubernetes.io/instance=<release-name>` to delete all PVCs that belong to a Helm release by matching their labels. Helm charts add labels to all their resources. For PostgreSQL, Bitnami applies something like the following. So the -l app.kubernetes.io/instance=<release-name> flag means "Delete every PVC where the label app.kubernetes.io/instance matches my Helm release name." 

```yaml
labels:
  app.kubernetes.io/name: postgresql
  app.kubernetes.io/instance: postgresql   # <--- release name
```


## Note

> Why is `@AllArgsConstructor` needed when using `@NoArgsConstructor` and `@Builder`?  
> `@Builder` generates an all-args constructor only if none exists. If you’ve defined `@NoArgsConstructor`, you must also define `@AllArgsConstructor`; otherwise, a compilation error like “actual and formal argument lists differ in length” may occur.
