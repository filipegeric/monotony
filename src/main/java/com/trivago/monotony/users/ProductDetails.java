package com.trivago.monotony.users;

public record ProductDetails(
        String productId,
        String name,
        String description,
        double price
) {
}
