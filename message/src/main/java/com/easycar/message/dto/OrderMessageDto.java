package com.easycar.message.dto;

public record OrderMessageDto(Long orderId, Long productId, String customerId) {}
