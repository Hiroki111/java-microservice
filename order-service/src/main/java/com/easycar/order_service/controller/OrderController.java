package com.easycar.order_service.controller;

import com.easycar.order_service.constants.RestApiConstants;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.OrderDto;
import com.easycar.order_service.dto.ResponseDto;
import com.easycar.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = "/api/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@RequestHeader("X-User-Id") String userId, @PathVariable Long id) {
        OrderDto order = orderService.findOrderById(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createOrder(
            @RequestHeader("easycar-correlation-id") String correlationId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody OrderCreateDto orderDto) {
        logger.debug("easycar-correlation-id found: {} ", correlationId);
        orderService.createOrder(correlationId, userId, orderDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(RestApiConstants.STATUS_201, RestApiConstants.MESSAGE_201));
    }
}
