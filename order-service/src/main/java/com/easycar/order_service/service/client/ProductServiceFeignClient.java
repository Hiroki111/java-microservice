package com.easycar.order_service.service.client;

import com.easycar.order_service.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("product-service")
public interface ProductServiceFeignClient {

    @GetMapping(value = "/api/products/{id}", consumes = "application/json")
    ResponseEntity<ProductDto> fetchProduct(
            @RequestHeader("easycar-correlation-id") String correlationId, @RequestParam Long id);
}
