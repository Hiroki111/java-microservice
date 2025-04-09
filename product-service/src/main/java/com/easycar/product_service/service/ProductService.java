package com.easycar.product_service.service;

import com.easycar.product_service.dto.ProductDto;
import com.easycar.product_service.entity.Product;
import com.easycar.product_service.exception.ResourceNotFoundException;
import com.easycar.product_service.mapper.ProductMapper;
import com.easycar.product_service.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", id.toString())
        );
    }

    public void createProduct(ProductDto productDto) {
        Product product = ProductMapper.mapProductDtoToProduct(productDto, new Product());
        productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductDto productDto) {
        Product currentProduct = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", id.toString())
        );
        Product updateProduct = ProductMapper.mapProductDtoToProduct(productDto, currentProduct);
        productRepository.save(updateProduct);

        return updateProduct;
    }
}
