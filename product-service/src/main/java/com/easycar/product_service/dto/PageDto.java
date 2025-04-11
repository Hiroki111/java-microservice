package com.easycar.product_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class PageDto<T> {
    List<T> content;
    int totalElements;
    int totalPages;
    int pageSize;
    int currentPage;
    boolean first;
    boolean last;
}
