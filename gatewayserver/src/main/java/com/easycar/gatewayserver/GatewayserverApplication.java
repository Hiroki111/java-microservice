package com.easycar.gatewayserver;

import java.time.LocalDateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }

    @Bean
    public RouteLocator easycarRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
                .routes()
                .route(p -> p.path("/easycar/order-service/**")
                        .filters(f -> f.rewritePath("/easycar/order-service/(?<segment>.*)", "/${segment}")
                                .addResponseHeader(
                                        "X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://ORDER-SERVICE"))
                .route(p -> p.path("/easycar/product-service/**")
                        .filters(f -> f.rewritePath("/easycar/product-service/(?<segment>.*)", "/${segment}")
                                .addResponseHeader(
                                        "X-Response-Time", LocalDateTime.now().toString()))
                        .uri("lb://PRODUCT-SERVICE"))
                .build();
    }
}
