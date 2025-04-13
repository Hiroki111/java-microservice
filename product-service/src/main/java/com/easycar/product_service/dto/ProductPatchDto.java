package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductPatchDto", description = "Schema to partially update product information")
public class ProductPatchDto {
    @Schema(nullable = true, example = "CR-V")
    @NotBlank(message = "name cannot be empty")
    private String name;

    @Schema(nullable = true, example = "Popular SUV")
    @NotBlank(message = "description cannot be empty")
    private String description;

    @Schema(nullable = true, example = "65000.50")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @Schema(nullable = true)
    private Boolean available;
}
