package com.easycar.product_service.functions;

import com.easycar.product_service.dto.OrderMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@Configuration
public class ProductFunctions {
    private static final Logger log = LoggerFactory.getLogger(ProductFunctions.class);

    @Bean
    public Consumer<OrderMessageDto> reserveProduct() {
        return orderMessageDto -> {
            log.info("Reserving a product for the order ID : " + orderMessageDto.toString());
            // TODO: Implement a function to find a product and make its "available" field false
        };
    }
}
