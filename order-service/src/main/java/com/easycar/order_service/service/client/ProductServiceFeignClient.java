package com.easycar.order_service.service.client;

import com.easycar.order_service.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "http://product-service:8081")
public interface ProductServiceFeignClient {

    @GetMapping(value = "/api/products/{id}", consumes = "application/json")
    ResponseEntity<ProductDto> fetchProduct(
            @RequestHeader("easycar-correlation-id") String correlationId, @PathVariable("id") Long id);
}
