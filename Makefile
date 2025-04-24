SERVICES = configserver product-service
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
		docker build -t $(DOCKER_REPO)-$$s:1.0.0 $$s; \
	done
