package com.easycar.product_service.functions;

import com.easycar.product_service.dto.OrderMessageDto;
import com.easycar.product_service.service.ProductService;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
public class ProductFunctions {
    private static final Logger log = LoggerFactory.getLogger(ProductFunctions.class);
    private final ProductService productService;

    public ProductFunctions(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Message<OrderMessageDto>> reserveProduct() {
        return message -> {
            log.info("Reserving a product : " + message.toString());
            productService.reserveProduct(message);
        };
    }
}
