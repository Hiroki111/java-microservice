package com.easycar.product_service.helper;

import com.easycar.product_service.domain.Category;
import com.easycar.product_service.domain.Make;
import com.easycar.product_service.domain.entity.Dealer;
import com.easycar.product_service.domain.entity.Product;
import java.math.BigDecimal;

public class EntityBuilder {
    public static Product.ProductBuilder buildDefaultProductBuilder(Dealer dealer) {
        return Product.builder()
                .name("Default Product")
                .description("Default Description")
                .price(BigDecimal.valueOf(100000))
                .category(Category.SEDAN)
                .make(Make.BMW)
                .mileage(1000)
                .dealer(dealer)
                .available(true);
    }
}
