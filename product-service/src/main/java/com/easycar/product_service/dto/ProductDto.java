package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductDto", description = "Schema to hold product information")
public class ProductDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "CR-V")
    @NotEmpty(message = "name can not be a null or empty")
    private String name;

    @Schema(example = "Popular SUV")
    @NotEmpty(message = "description can not be a null or empty")
    private String description;

    @Schema(example = "65000.50")
    @PositiveOrZero(message = "The value must be positive or zero")
    @NotNull(message = "price can not be a null")
    private BigDecimal price;

    private boolean available;
}
