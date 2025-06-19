package com.easycar.product_service.controller;

import com.easycar.product_service.dto.*;
import com.easycar.product_service.service.ProductService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
}
