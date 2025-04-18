package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "DealerPatchDto", description = "Schema to partially update dealer information")
public class DealerPatchDto {
    @Schema(nullable = true, example = "AAA Auto")
    @Pattern(regexp = "^(?!\\s*$).+", message = "name must not be blank")
    private String name;

    @Schema(nullable = true, example = "Peach street 123")
    @Pattern(regexp = "^(?!\\s*$).+", message = "address must not be blank")
    private String address;
}
