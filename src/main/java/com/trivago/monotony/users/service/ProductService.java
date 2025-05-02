package com.trivago.monotony.users.service;

import com.trivago.monotony.users.ProductDetails;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<ProductDetails> findProductById(String productId);
}
