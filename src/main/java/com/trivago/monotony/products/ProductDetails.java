package com.trivago.monotony.products;

import java.util.List;

public record ProductDetails(Product product, List<Product> recommendations) {
}
