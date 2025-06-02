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

    @Schema(example = "John Smith")
    private String customerName;
}
