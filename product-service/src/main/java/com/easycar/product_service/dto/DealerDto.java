package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "DealerDto", description = "Schema to hold dealer information")
@Builder
public class DealerDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Alpha Auto")
    private String name;

    @Schema(example = "Sunny Street 10-20")
    private String address;
}
