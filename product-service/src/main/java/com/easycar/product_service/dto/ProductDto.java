package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductDto", description = "Schema to hold product information")
public class ProductDto {

    private Long id;

    @NotEmpty(message = "name can not be a null or empty")
    private String name;

    @NotEmpty(message = "description can not be a null or empty")
    private String description;

    @Min(value = 0, message = "The value must be positive")
    @NotNull(message = "price can not be a null")
    private BigDecimal price;

    private boolean available;
}
