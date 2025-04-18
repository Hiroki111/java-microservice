package com.easycar.product_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.sql.init.mode=never"})
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {}
}
