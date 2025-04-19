package com.easycar.product_service.mapper;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductCreateDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public class ProductMapper {

    public static Product mapProductCreateDtoToProduct(ProductCreateDto productDto, Dealer dealer) {
        return Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .available(productDto.getAvailable())
                .category(productDto.getCategory())
                .dealer(dealer)
                .build();
    }

    public static ProductDto mapProductToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .available(product.isAvailable())
                .category(product.getCategory())
                .build();
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

    public static void updateProductFromPatchDto(Product product, ProductPatchDto productPatchDto) {

        if (productPatchDto.getName() != null) {
            product.setName(productPatchDto.getName());
        }
        if (productPatchDto.getDescription() != null) {
            product.setDescription(productPatchDto.getDescription());
        }
        if (productPatchDto.getPrice() != null) {
            product.setPrice(productPatchDto.getPrice());
        }
        if (productPatchDto.getAvailable() != null) {
            product.setAvailable(productPatchDto.getAvailable());
        }
    }

    public static void updateProductFromPatchDto(Product product, ProductPatchDto productPatchDto, Dealer newDealer) {
        updateProductFromPatchDto(product, productPatchDto);
        if (newDealer != null) {
            product.setDealer(newDealer);
        }
    }
}
