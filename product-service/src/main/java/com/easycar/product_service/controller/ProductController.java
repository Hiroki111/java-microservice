package com.easycar.product_service.controller;

import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.dto.ResponseDto;
import com.easycar.product_service.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        ProductDto product = productService.findProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductDto>> getProducts(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 100) Pageable pageable) {
        PageDto<ProductDto> products = productService.findProducts(minPrice, maxPrice, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createProduct(@Valid @RequestBody ProductDto productDto) {
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
