package com.easycar.gatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@SuppressWarnings("unused")
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        // Product service
                        .pathMatchers(HttpMethod.GET, "/easycar/product-service/api/products").hasRole("INTERNAL_USER")
                        .pathMatchers(HttpMethod.POST, "/easycar/product-service/api/products").hasRole("INTERNAL_USER")
                        .pathMatchers(HttpMethod.PATCH, "/easycar/product-service/api/products/*").hasRole("INTERNAL_USER")
                        .pathMatchers(HttpMethod.DELETE, "/easycar/product-service/api/products/*").hasRole("INTERNAL_USER")
                        .pathMatchers(HttpMethod.POST, "/easycar/product-service/api/dealers").hasRole("INTERNAL_USER")
                        // Order service
                        .pathMatchers(HttpMethod.GET, "/easycar/order-service/api/orders").hasRole("INTERNAL_USER")
                        .pathMatchers(HttpMethod.GET, "/easycar/order-service/api/orders/*").hasAnyRole("INTERNAL_USER", "CUSTOMER") // Note: Ownership check inside service
                        .pathMatchers(HttpMethod.POST, "/easycar/order-service/api/orders").hasAnyRole("INTERNAL_USER", "CUSTOMER")
                        .pathMatchers(HttpMethod.DELETE, "/easycar/order-service/api/orders/*").hasRole("INTERNAL_USER")
                        // Default fallback
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
