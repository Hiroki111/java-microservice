package com.easycar.order_service.controller;

import com.easycar.order_service.constants.RestApiConstants;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.OrderDto;
import com.easycar.order_service.dto.PageDto;
import com.easycar.order_service.dto.ResponseDto;
import com.easycar.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

    @GetMapping
    @Parameters({
            @Parameter(
                    name = "page",
                    description = "page number (0-based)",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(
                    name = "size",
                    description = "page size",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", defaultValue = "100")),
            @Parameter(
                    name = "sort",
                    description = "sort specification by comma-separated value (e.g. 'id,desc')",
                    in = ParameterIn.QUERY,
                    schema = @Schema(type = "array"),
                    explode = Explode.FALSE,
                    style = ParameterStyle.SIMPLE),
    })
    public ResponseEntity<PageDto<OrderDto>> getOrders(
            @ParameterObject @PageableDefault(size = 100) Pageable pageable) {
        PageDto<OrderDto> orders = orderService.findOrders(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createOrder(
            @RequestHeader("easycar-correlation-id") String correlationId,
            @Valid @RequestBody OrderCreateDto orderDto) {
        logger.debug("easycar-correlation-id found: {} ", correlationId);
        orderService.createOrder(correlationId, orderDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(RestApiConstants.STATUS_201, RestApiConstants.MESSAGE_201));
    }
}
