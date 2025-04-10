package com.easycar.product_service.mapper;

import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.entity.Product;
import java.math.BigDecimal;

public class ProductMapper {

    public static Product mapProductDtoToProduct(ProductDto productDto, Product product) {
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setAvailable(productDto.isAvailable());
        return product;
    }

    public static Product mapProductPatchDtoToProduct(ProductPatchDto productPatchDto, Product product) {
        if (productPatchDto.getName() != null) {
            product.setName(productPatchDto.getName());
        }
        if (productPatchDto.getDescription() != null) {
            product.setDescription(productPatchDto.getDescription());
        }
        if (productPatchDto.getPrice() != null) {
            if (productPatchDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be non-negative");
            }
            product.setPrice(productPatchDto.getPrice());
        }
        if (productPatchDto.getAvailable() != null) {
            product.setAvailable(productPatchDto.getAvailable());
        }
        return product;
    }
}
