package com.easycar.order_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "OrderCreateDto", description = "Schema to create a new order")
@Builder
public class OrderCreateDto {
    @Schema(example = "1")
    @Min(value = 1)
    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @Schema(example = "John Smith")
    @NotBlank(message = "Customer name cannot be empty")
    private String customerName;
}
