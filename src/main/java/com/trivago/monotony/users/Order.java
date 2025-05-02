package com.trivago.monotony.users;

public record Order(
        String orderId,
        String userId,
        String primaryProductId,
        double amount,
        OrderStatus status
) {
}
