package com.easycar.order_service.controller;

import com.easycar.order_service.constants.RestApiConstants;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.ResponseDto;
import com.easycar.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/api/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class OrderController {
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ResponseDto> createOrder(@Valid @RequestBody OrderCreateDto orderDto) {
        orderService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(RestApiConstants.STATUS_201, RestApiConstants.MESSAGE_201));
    }
}
