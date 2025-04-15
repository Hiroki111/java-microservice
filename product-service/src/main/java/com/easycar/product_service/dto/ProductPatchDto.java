package com.easycar.product_service.dto;

import com.easycar.product_service.constants.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "ProductPatchDto", description = "Schema to partially update product information")
public class ProductPatchDto {
    @Schema(nullable = true, example = "CR-V")
    @Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    private String name;

    @Schema(nullable = true, example = "Popular SUV")
    @Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    private String description;

    @Schema(nullable = true, example = "65000.50")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @Schema(nullable = true)
    private Boolean available;

    @Schema(nullable = true, example = "SUV")
    private Category category;

    @Schema(example = "1")
    @Min(value = 1)
    private Long dealerId;
}
