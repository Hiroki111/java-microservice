package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(name = "Page", description = "Schema to hold paginated resources")
@Builder
public class PageDto<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    List<T> content;

    @Schema(example = "1")
    int totalElements;

    @Schema(example = "1")
    int totalPages;

    @Schema(example = "100")
    int pageSize;

    @Schema(example = "0")
    int currentPage;

    @Schema(example = "true")
    boolean first;

    @Schema(example = "true")
    boolean last;
}
