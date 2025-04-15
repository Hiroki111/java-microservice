package com.easycar.product_service.dto;

import com.easycar.product_service.constants.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(name = "ProductCreateDto", description = "Schema to create a new product")
public class ProductCreateDto {
    @Schema(example = "CR-V")
    @NotBlank(message = "name cannot be empty")
    private String name;

    @Schema(example = "Popular SUV")
    @NotBlank(message = "description cannot be empty")
    private String description;

    @Schema(example = "65000.50")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    @Schema(description = "defaults to false if omitted")
    private Boolean available;

    @Schema(example = "SUV")
    private Category category;

    @Schema(example = "1")
    @Min(value = 1)
    private Long dealerId;
}
