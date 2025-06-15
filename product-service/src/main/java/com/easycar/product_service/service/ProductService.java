package com.easycar.product_service.service;

import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.dto.*;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
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

        if (productPatchDto.getDealerId() != null) {
            Dealer newDealer = dealerRepository
                    .findById(productPatchDto.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Dealer", "id", productPatchDto.getDealerId().toString()));
            ProductMapper.updateProductFromPatchDto(product, productPatchDto, newDealer);
        } else {
            ProductMapper.updateProductFromPatchDto(product, productPatchDto);
        }

        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        productRepository.delete(product);
    }

    public void reserveProduct(Message<OrderMessageDto> message) {
        var orderMessageDto = message.getPayload();
        productRepository.findById(orderMessageDto.productId()).ifPresent(product -> {
            if (!product.isAvailable()) {
                log.info("Product " + orderMessageDto.productId() + " already reserved, skipping.");
                return;
            }
            product.setAvailable(false);
            productRepository.save(product);
        });
    }
}
