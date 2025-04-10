package com.easycar.product_service.service;

import com.easycar.product_service.dto.PageDto;
import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.dto.ProductPatchDto;
import com.easycar.product_service.entity.Product;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    public Product findProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id.toString()));
    }

    public PageDto<ProductDto> findProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return ProductMapper.mapProductPageToPageDto(productPage);
    }

    public void createProduct(ProductDto productDto) {
        Product product = ProductMapper.mapProductDtoToProduct(productDto, new Product());
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
