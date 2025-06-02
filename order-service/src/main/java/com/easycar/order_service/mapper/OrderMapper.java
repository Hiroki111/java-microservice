package com.easycar.order_service.mapper;

import com.easycar.order_service.domain.entity.Order;
import com.easycar.order_service.dto.OrderCreateDto;
import com.easycar.order_service.dto.OrderDto;
import com.easycar.order_service.dto.PageDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderDto mapOrderToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .customerName(order.getCustomerName())
                .build();
    }

    public static PageDto<OrderDto> mapOrderPageToOrderDto(Page<Order> orderPage) {
        List<OrderDto> content = orderPage.getContent().stream()
                .map(OrderMapper::mapOrderToOrderDto)
                .collect(Collectors.toList());

        return PageDto.<OrderDto>builder()
                .content(content)
                .totalElements((int) orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .pageSize(orderPage.getSize())
                .currentPage(orderPage.getNumber())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .build();
    }

    public static Order mapOrderCreateDtoToOrder(OrderCreateDto orderCreateDto) {
        return Order.builder()
                .productId(orderCreateDto.getProductId())
                .customerName(orderCreateDto.getCustomerName())
                .build();
    }
}
