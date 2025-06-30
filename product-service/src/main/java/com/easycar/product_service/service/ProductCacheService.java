package com.easycar.product_service.service;

import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductDto;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ProductCacheService {
    private final ProductService productService;

    public ProductCacheService(@Lazy ProductService productService) {
        this.productService = productService;
    }

    @CachePut(value = "defaultProducts", key = "'default'")
    public PageDto<ProductDto> refreshDefaultProducts() {
        return productService.findProductsForPublic(null, null, null, null, null, null, null, ProductConstants.DEFAULT_PRODUCT_PAGEABLE);
    }
}
