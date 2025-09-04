# -*- mode: Python -*-
# Tiltfile for EasyCar microservices project

docker_build(
	"hiroki111/easycar-configserver",
	context="./configserver",
	dockerfile="./configserver/Dockerfile",
	live_update=[
		sync("configserver/target/configserver-0.0.1-SNAPSHOT.jar", "/app/configserver.jar"),
	]
)
docker_build(
	"hiroki111/easycar-gatewayserver",
	context="./gatewayserver",
	dockerfile="./gatewayserver/Dockerfile",
	live_update=[
		sync("gatewayserver/target/gatewayserver-0.0.1-SNAPSHOT.jar", "/app/gatewayserver.jar"),
	]
)
docker_build(
	"hiroki111/easycar-message",
	context="./message",
	dockerfile="./message/Dockerfile",
	live_update=[
		sync("message/target/message-0.0.1-SNAPSHOT.jar", "/app/message.jar"),
	]
)
docker_build(
	"hiroki111/easycar-order-service",
	context="./order-service",
	dockerfile="./order-service/Dockerfile",
	live_update=[
		sync("order-service/target/order-service-0.0.1-SNAPSHOT.jar", "/app/order-service.jar"),
	]
)
docker_build(
	"hiroki111/easycar-product-service",
	context="./product-service",
	dockerfile="./product-service/Dockerfile",
	live_update=[
		sync("product-service/target/product-service-0.0.1-SNAPSHOT.jar", "/app/product-service.jar"),
	]
)


# TODO: Use k8s_yaml
# k8s_yaml(helm('helm/environments/dev'))
k8s_yaml(local('helm template helm/environments/dev'))
# watch_file('helm/environments/dev')

# TODO: Restore this
# Label logs in the Tilt UI by service name
# for name in SERVICES.keys():
#     local_resource(
#         f"logs-{name}",
#         "echo watching logs",
#         serve_cmd=f"kubectl logs -l app={name} -f",
#         deps=[SERVICES[name]["path"]],
#     )
