package com.easycar.product_service.service;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductCreateDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;
    private DealerRepository dealerRepository;

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

    public void createProduct(ProductCreateDto productDto) {
        Dealer dealer = dealerRepository
                .findById(productDto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer", "id", productDto.getDealerId().toString()));
        Product product = ProductMapper.mapProductCreateDtoToProduct(productDto, dealer);
        productRepository.save(product);
    }

    public void patchProduct(Long id, ProductPatchDto productPatchDto) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        Optional<Dealer> newDealerOption = Optional.empty();

        if (productPatchDto.getDealerId() != null) {
            newDealerOption = dealerRepository.findById(productPatchDto.getDealerId());
            if (newDealerOption.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Dealer", "id", productPatchDto.getDealerId().toString());
            }
        }

        ProductMapper.updateProductFromPatchDto(product, productPatchDto, newDealerOption);
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        productRepository.delete(product);
    }
}
