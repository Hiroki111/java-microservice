package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "DealerCreateDto", description = "Schema to create a new dealer")
@Builder
public class DealerCreateDto {
    @Schema(example = "Sunshine Auto")
    @NotBlank(message = "name cannot be empty")
    private String name;

    @Schema(example = "Peach Street 123")
    @NotBlank(message = "address cannot be empty")
    private String address;
}
