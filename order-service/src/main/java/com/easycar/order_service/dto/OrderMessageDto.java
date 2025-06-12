package com.easycar.order_service.dto;

public record OrderMessageDto(Long orderId, Long productId, String customerId) {
}
