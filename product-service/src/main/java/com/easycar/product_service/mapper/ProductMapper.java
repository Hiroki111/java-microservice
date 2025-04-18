package com.easycar.product_service.mapper;

import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductCreateDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import java.util.List;
import java.util.Optional;
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
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setAvailable(product.isAvailable());
        productDto.setCategory(product.getCategory());

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

    public static void updateProductFromPatchDto(
            Product product, ProductPatchDto productPatchDto, Optional<Dealer> newDealerOption) {

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
        if (newDealerOption.isPresent()) {
            Dealer newDealer = newDealerOption.get();
            product.setDealer(newDealer);
        }
    }
}
