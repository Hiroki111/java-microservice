package com.easycar.product_service.service;

import com.easycar.product_service.constants.CacheConstants;
import com.easycar.product_service.constants.ProductConstants;
import com.easycar.product_service.domain.Make;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import com.easycar.product_service.dto.OrderMessageDto;
import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductCreateDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.DealerRepository;
import com.easycar.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "price", "mileage");
    private ProductRepository productRepository;
    private DealerRepository dealerRepository;
    private ProductCacheService productCacheService;

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

    public PageDto<ProductDto> findProductsForPublic(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minMileage,
            BigDecimal maxMileage,
            String name,
            List<Make> makes,
            List<Long> dealerIds,
            Pageable pageable) {

        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException("Sorting by '" + order.getProperty()
                        + "' is not allowed. Allowed properties: " + ALLOWED_SORT_PROPERTIES);
            }
        }

        Specification<Product> spec =
                Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("available")));

        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (minMileage != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("mileage"), minMileage));
        }
        if (maxMileage != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("mileage"), maxMileage));
        }
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (makes != null && !makes.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> root.get("make").in(makes));
        }
        if (dealerIds != null && !dealerIds.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("dealer").get("id").in(dealerIds));
        }

        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return ProductMapper.mapProductPageToPageDto(productPage);
    }

    @Cacheable(value = CacheConstants.DEFAULT_PRODUCTS_CACHE, unless = "#result == null")
    public PageDto<ProductDto> getDefaultProducts() {
        return this.findProductsForPublic(
                null, null, null, null, null, null, null, ProductConstants.DEFAULT_PRODUCT_PAGEABLE);
    }

    public void createProduct(ProductCreateDto productDto) {
        Dealer dealer = dealerRepository
                .findById(productDto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer", "id", productDto.getDealerId().toString()));
        Product product = ProductMapper.mapProductCreateDtoToProduct(productDto, dealer);
        productRepository.save(product);
        productCacheService.refreshDefaultProducts();
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
        productCacheService.refreshDefaultProducts();
    }

    public void deleteProduct(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
        productRepository.delete(product);
        productCacheService.refreshDefaultProducts();
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
            productCacheService.refreshDefaultProducts();
        });
    }
}
