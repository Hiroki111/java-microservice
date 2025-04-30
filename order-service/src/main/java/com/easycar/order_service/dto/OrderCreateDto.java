package com.easycar.order_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "OrderCreateDto", description = "Schema to create a new order")
public class OrderCreateDto {
    @Schema(example = "1")
    @Min(value = 1)
    private Long productId;

    // TODO: Replace it with customer ID
    @Schema(example = "John Smith")
    @NotBlank(message = "customerName cannot be empty")
    private String customerName;
}
