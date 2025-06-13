package com.easycar.product_service.dto;

public record OrderMessageDto(Long orderId, Long productId, String customerId) {
}
