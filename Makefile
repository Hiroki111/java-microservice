SERVICES = configserver gatewayserver order-service product-service message
DOCKER_REPO = hiroki111/easycar

.PHONY: all build-jar build-images

all: build-jar build-images

build-jar:
	@for s in $(SERVICES); do \
		echo "🔨 Building $$s..."; \
		(cd $$s && mvn clean install); \
	done

build-images:
	@for s in $(SERVICES); do \
		echo "🐳 Building Docker image for $$s..."; \
		docker build -t $(DOCKER_REPO)-$$s:2.0.0 $$s; \
	done

push-images:
	@for s in $(SERVICES); do \
		echo "🐳 Pushing Docker image for $$s..."; \
		docker image push docker.io/$(DOCKER_REPO)-$$s:2.0.0; \
	done