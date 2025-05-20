package com.easycar.order_service.service;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.ProductDto;
import com.easycar.order_service.exception.DownstreamServiceUnavailableException;
import com.easycar.order_service.exception.ProductUnavailableException;
import com.easycar.order_service.exception.ResourceNotFoundException;
import com.easycar.order_service.mapper.OrderMapper;
import com.easycar.order_service.repository.OrderRepository;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;
    private ProductServiceFeignClient productServiceFeignClient;

    public void createOrder(String correlationId, OrderCreateDto orderDto) {
        ResponseEntity<ProductDto> responseEntity;
        Long productId = orderDto.getProductId();
        try {
            responseEntity = productServiceFeignClient.fetchProduct(correlationId, productId);
        } catch (NoFallbackAvailableException ex) {
            if (ex.getCause() instanceof FeignException.NotFound) {
                throw new ResourceNotFoundException("Product", "id", productId.toString());
            }
            throw new DownstreamServiceUnavailableException("product-service");
        }

        ProductDto productDto = responseEntity.getBody();
        if (productDto == null || !productDto.isAvailable()) {
            throw new ProductUnavailableException(productId.toString());
        }

        Order order = OrderMapper.mapOrderCreateDtoToOrder(orderDto);
        orderRepository.save(order);
    }
}
