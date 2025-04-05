package com.easycar.product_service.controller;

import com.easycar.product_service.entity.Product;
import com.easycar.product_service.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
@Validated
public class ProductController {

    private ProductRepository productRepository;


}