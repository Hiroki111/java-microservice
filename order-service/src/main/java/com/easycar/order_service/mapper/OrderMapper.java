package com.easycar.order_service.mapper;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.OrderCreateDto;

public class OrderMapper {
    public static Order mapOrderCreateDtoToOrder(OrderCreateDto orderCreateDto) {
        return Order.builder().
                productId(orderCreateDto.getProductId()).
                customerName(orderCreateDto.getCustomerName()).
                build();
    }
}
