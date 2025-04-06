package com.easycar.product_service.mapper;

import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.entity.Product;

public class ProductMapper {

    public static Product mapProductDtoToProduct(ProductDto productDto, Product product) {
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setAvailable(productDto.isAvailable());
        return product;
    }
}
