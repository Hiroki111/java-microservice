SERVICES = configserver gatewayserver order-service product-service message
DOCKER_REPO = hiroki111/easycar

.PHONY: all build-jar build-images

all: build-jar

build-jar:
	@for s in $(SERVICES); do \
		echo "🔨 Building $$s..."; \
		(cd $$s && mvn clean install); \
	done

install-infra-helm:
	helm install order-service-db helm/order-service-db
	helm install product-service-db helm/product-service-db
	helm install rabbitmq helm/rabbitmq
	helm install redis helm/redis/

uninstall-infra-helm:
	helm uninstall order-service-db
	helm uninstall product-service-db
	helm uninstall rabbitmq
	helm uninstall redis