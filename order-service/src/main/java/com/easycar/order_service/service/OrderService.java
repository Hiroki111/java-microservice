package com.easycar.order_service.service;

import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.ProductDto;
import com.easycar.order_service.exception.ProductUnavailableException;
import com.easycar.order_service.exception.ResourceNotFoundException;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {

    private ProductServiceFeignClient productServiceFeignClient;

    public void createOrder(OrderCreateDto orderDto) {
        ResponseEntity<ProductDto> responseEntity;
        Long productId = orderDto.getProductId();
        try {
            responseEntity = productServiceFeignClient.fetchProduct(productId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Product", "id", productId.toString());
        }

        ProductDto productDto = responseEntity.getBody();
        if (!productDto.isAvailable()) {
            throw new ProductUnavailableException(productId.toString());
        }
        System.out.println("product found: id " + productDto.getId());
        // The rest of the logic
    }
}
