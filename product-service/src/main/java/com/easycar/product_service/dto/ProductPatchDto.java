package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductPatchDto", description = "Schema to partially update product information")
public class ProductPatchDto {
    @Pattern(regexp = "^.+$", message = "name can not be empty")
    private String name;

    @Pattern(regexp = "^.+$", message = "description can not be empty")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    private Boolean available;
}
