package com.easycar.order_service.service;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.*;
import com.easycar.order_service.exception.AccessDeniedException;
import com.easycar.order_service.exception.DownstreamServiceUnavailableException;
import com.easycar.order_service.exception.ProductUnavailableException;
import com.easycar.order_service.exception.ResourceNotFoundException;
import com.easycar.order_service.mapper.OrderMapper;
import com.easycar.order_service.repository.OrderRepository;
import com.easycar.order_service.service.client.ProductServiceFeignClient;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private OrderRepository orderRepository;
    private ProductServiceFeignClient productServiceFeignClient;
    private final StreamBridge streamBridge;

    public OrderDto findOrderById(Long id, String userId) {
        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id.toString()));

        if (!order.getCustomerId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to view this order.");
        }

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
        sendCommunication(order);
    }

    private void sendCommunication(Order order) {
        var orderMessageDto = new OrderMessageDto(order.getId(), order.getProductId(),
                order.getCustomerId());
        log.info("Sending Communication request for the details: {}", orderMessageDto);
        var sendCommunicationTriggered = streamBridge.send("sendCommunication-out-0", orderMessageDto);
        log.info("Is the Communication request successfully triggered ? : {}", sendCommunicationTriggered);

        log.info("Sending Product reservation request for the details: {}", orderMessageDto);
        var reserveProductTriggered = streamBridge.send("reserveProduct-out-0", orderMessageDto);
        log.info("Is the Product reservation request successfully triggered ? : {}", reserveProductTriggered);
    }
}
