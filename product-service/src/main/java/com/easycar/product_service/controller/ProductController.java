package com.easycar.product_service.controller;

import com.easycar.product_service.domain.Make;
import com.easycar.product_service.dto.*;
import com.easycar.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = "/api/products",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(
            @RequestHeader("easycar-correlation-id") String correlationId, @PathVariable Long id) {
        logger.debug("easycar-correlation-id found: {} ", correlationId);
        ProductDto product = productService.findProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    // 1) The endpoint returns the latest 10 available products
    // 2) The endpoint filters the output by price range, mileage range, makes, car name, dealer IDs
    // 3) Cache the result once a day via cron job or something similar
    // 4) Update the cache when the available status of any latest 10 products changes
    // 5) When the endpoint receives a request that returns at least one product, log the request. The logs will be used
    // for analyzing which search criteria are popular, and
    @GetMapping
    @Parameters({
        @Parameter(
                name = "page",
                description = "page number (0-based)",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(
                name = "sort",
                description = "sort by one of: createdAt, price, mileage (e.g. 'price,asc')",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "array"),
                explode = Explode.FALSE,
                style = ParameterStyle.SIMPLE),
    })
    public ResponseEntity<PageDto<ProductDto>> getProductsForPublic(
            @Parameter(description = "Min price (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Max price (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Min mileage (inclusive)") @RequestParam(required = false) BigDecimal minMileage,
            @Parameter(description = "Max mileage (inclusive)") @RequestParam(required = false) BigDecimal maxMileage,
            @Parameter(description = "Name (case insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Makes") @RequestParam(required = false) List<Make> makes,
            @Parameter(description = "Dealer IDs") @RequestParam(required = false) List<Long> dealerIds,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        if (isDefaultSearch(minPrice, maxPrice, minMileage, maxMileage, name, makes, dealerIds, pageable)) {
            return ResponseEntity.ok(productService.getDefaultProducts());
        }
        PageDto<ProductDto> products = productService.findProductsForPublic(
                minPrice, maxPrice, minMileage, maxMileage, name, makes, dealerIds, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    private boolean isDefaultSearch(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minMileage,
            BigDecimal maxMileage,
            String name,
            List<Make> makes,
            List<Long> dealerIds,
            Pageable pageable) {
        return minPrice == null
                && maxPrice == null
                && minMileage == null
                && maxMileage == null
                && (name == null || name.isBlank())
                && (makes == null || makes.isEmpty())
                && (dealerIds == null || dealerIds.isEmpty())
                && pageable.getPageNumber() == 0
                && pageable.getPageSize() == 10
                && (!pageable.getSort().isSorted()
                        || pageable.getSort().getOrderFor("createdAt") != null
                                && Objects.requireNonNull(pageable.getSort().getOrderFor("createdAt"))
                                                .getDirection()
                                        == Sort.Direction.DESC);
    }
}
