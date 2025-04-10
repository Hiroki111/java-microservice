package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductDto", description = "Schema to partially update product information")
public class ProductPatchDto {
    @Pattern(regexp = "^.+$", message = "name can not be empty")
    private String name;

    @Pattern(regexp = "^.+$", message = "description can not be empty")
    private String description;

    @Min(value = 0, message = "The value must be positive")
    private BigDecimal price;

    private Boolean available;
}
