package com.easycar.product_service.controller.backstage;

import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.*;
import com.easycar.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = "/api/backstage/products",
        produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Validated
@SuppressWarnings("unused")
public class BackstageProductController {
    private static final Logger logger = LoggerFactory.getLogger(BackstageProductController.class);
    private ProductService productService;

    @GetMapping
    @Parameters({
        @Parameter(
                name = "page",
                description = "page number (0-based)",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(
                name = "size",
                description = "page size",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "integer", defaultValue = "100")),
        @Parameter(
                name = "sort",
                description = "sort specification by comma-separated value (e.g. 'price,asc')",
                in = ParameterIn.QUERY,
                schema = @Schema(type = "array"),
                explode = Explode.FALSE,
                style = ParameterStyle.SIMPLE),
    })
    public ResponseEntity<PageDto<ProductDto>> getProducts(
            @Parameter(description = "Min price (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Max price (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @ParameterObject @PageableDefault(size = 100) Pageable pageable) {
        PageDto<ProductDto> products = productService.findProducts(minPrice, maxPrice, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createProduct(@Valid @RequestBody ProductCreateDto productDto) {
        productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(ProductConstants.STATUS_201, ProductConstants.MESSAGE_201));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto> patchProduct(
            @PathVariable Long id, @Valid @RequestBody ProductPatchDto productDto) {
        productService.patchProduct(id, productDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(ProductConstants.STATUS_200, ProductConstants.MESSAGE_200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto(ProductConstants.STATUS_200, ProductConstants.MESSAGE_200));
    }
}
