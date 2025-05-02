package com.trivago.monotony.users;

public record EnrichedOrder(
        String orderId,
        double amount,
        OrderStatus status,
        ProductDetails product,
        String productFetchError
) {

    public static EnrichedOrder fromOrderAndProduct(Order order, ProductDetails product) {
        return new EnrichedOrder(order.orderId(), order.amount(), order.status(), product, null);
    }

    public static EnrichedOrder fromOrderWithError(Order order, String error) {
        return new EnrichedOrder(order.orderId(), order.amount(), order.status(), null, error);
    }
}
