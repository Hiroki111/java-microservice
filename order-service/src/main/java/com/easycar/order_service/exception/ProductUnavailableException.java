package com.easycar.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(String productId) {
        super(String.format("Product ID %s not available", productId));
    }
}
