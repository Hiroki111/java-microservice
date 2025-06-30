package com.easycar.product_service.constants;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// TODO: Rename this so that this class can be used for other resources
public class ProductConstants {
    private ProductConstants() {
        // restrict instantiation
    }

    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "Product created successfully";
    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "Request processed successfully";

    public static final Pageable DEFAULT_PRODUCT_PAGEABLE =
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
}
