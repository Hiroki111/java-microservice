package com.easycar.order_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "OrderDto", description = "Schema to hold order information")
@Builder
public class OrderDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "2")
    private Long productId;

    @Schema(example = "b8efe803-9d58-4229-854f-63b36da80b6c")
    private String customerId;

    @Schema(example = "John Smith")
    private String customerName;
}
