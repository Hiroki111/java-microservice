package com.easycar.order_service.service;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.OrderDto;
import com.easycar.order_service.dto.PageDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {
    private OrderRepository orderRepository;
    private ProductServiceFeignClient productServiceFeignClient;

    public OrderDto findOrderById(Long id) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id.toString()));
        return OrderMapper.mapOrderToOrderDto(order);
    }

    public PageDto<OrderDto> findOrders(Pageable pageable) {
        Specification<Order> spec = Specification.where(null);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        return OrderMapper.mapOrderPageToOrderDto(orderPage);
    }

    public void createOrder(String correlationId, String userId, OrderCreateDto orderDto) {
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
        order.setCustomerId(userId);
        orderRepository.save(order);
    }
}
