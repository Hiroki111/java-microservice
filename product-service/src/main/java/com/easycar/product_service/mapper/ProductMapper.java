package com.easycar.product_service.mapper;

import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public class ProductMapper {

    public static Product mapProductDtoToProduct(ProductDto productDto, Product product) {
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setAvailable(productDto.isAvailable());
        return product;
    }

    public static Product mapProductPatchDtoToProduct(ProductPatchDto productPatchDto, Product currentProduct) {
        Product newProduct = Product.builder()
                .id(currentProduct.getId())
                .name(currentProduct.getName())
                .description(currentProduct.getDescription())
                .price(currentProduct.getPrice())
                .available(currentProduct.isAvailable())
                .build();

        if (productPatchDto.getName() != null) {
            newProduct.setName(productPatchDto.getName());
        }
        if (productPatchDto.getDescription() != null) {
            newProduct.setDescription(productPatchDto.getDescription());
        }
        if (productPatchDto.getPrice() != null) {
            if (productPatchDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be non-negative");
            }
            newProduct.setPrice(productPatchDto.getPrice());
        }
        if (productPatchDto.getAvailable() != null) {
            newProduct.setAvailable(productPatchDto.getAvailable());
        }
        return newProduct;
    }

    public static ProductDto mapProductToProductDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setAvailable(product.isAvailable());

        return productDto;
    }

    public static PageDto<ProductDto> mapProductPageToPageDto(Page<Product> productPage) {
        List<ProductDto> content = productPage.getContent().stream()
                .map(ProductMapper::mapProductToProductDto)
                .collect(Collectors.toList());

        return PageDto.<ProductDto>builder()
                .content(content)
                .totalElements((int) productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .pageSize(productPage.getSize())
                .currentPage(productPage.getNumber())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }
}
