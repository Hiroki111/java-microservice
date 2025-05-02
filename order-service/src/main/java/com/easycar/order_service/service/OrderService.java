package com.easycar.order_service.service;

import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.ProductDto;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {

    private ProductServiceFeignClient productServiceFeignClient;

    public void createOrder(OrderCreateDto orderDto) {
        ResponseEntity<ProductDto> responseEntity = productServiceFeignClient.fetchProduct(orderDto.getProductId());
        ProductDto productDto = responseEntity.getBody();
        if (productDto == null) {
            // TODO: Replace it with ResourceNotFoundException
            throw new Error("Product not found");
        }
        if (!productDto.isAvailable()) {
            // TODO: Replace it with ResourceNotFoundException
            throw new Error("Product not available");
        }
        System.out.println("product found: id " + productDto.getId());
        // The rest of the logic
    }
}
