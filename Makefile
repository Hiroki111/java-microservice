SERVICES = configserver gatewayserver order-service product-service message

.PHONY: all build-jar

all: build-jar

build-jar:
	@for s in $(SERVICES); do \
		echo "🔨 Building $$s..."; \
		(cd $$s && mvn clean install); \
	done

build-image-to-registry:
	@for s in $(SERVICES); do \
		echo "🔨 Building $$s..."; \
		(cd $$s && mvn compile jib:build); \
	done

build-image-to-docker-deamon:
	@for s in $(SERVICES); do \
		echo "🔨 Building $$s..."; \
		(cd $$s && mvn compile jib:dockerBuild); \
	done

install-infra-helm:
	helm install order-service-db bitnami/postgresql --version 16.7.27 --values helm/external/order-service-db/values.yaml
	helm install product-service-db bitnami/postgresql --version 16.7.27 --values helm/external/product-service-db/values.yaml
	helm install rabbitmq bitnami/rabbitmq --version 16.0.14 --values helm/external/rabbitmq/values.yaml
	helm install redis bitnami/redis --version 22.0.7 --values helm/external/redis/values.yaml

uninstall-infra-helm:
	helm uninstall order-service-db
	helm uninstall product-service-db
	helm uninstall rabbitmq
	helm uninstall redis