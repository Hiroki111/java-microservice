package com.easycar.product_service.service;

import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.entity.Product;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    public ProductDto findProductById(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        return ProductMapper.mapProductToProductDto(product);
    }

    public PageDto<ProductDto> findProducts(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return ProductMapper.mapProductPageToPageDto(productPage);
    }

    public void createProduct(ProductDto productDto) {
        Product product = ProductMapper.mapProductDtoToProduct(productDto);
        productRepository.save(product);
    }

    public void patchProduct(Long id, ProductPatchDto productPatchDto) {
        Product currentProduct = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        Product updateProduct = ProductMapper.mapProductPatchDtoToProduct(productPatchDto, currentProduct);
        productRepository.save(updateProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        productRepository.delete(product);
    }
}
