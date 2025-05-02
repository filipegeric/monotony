package com.trivago.monotony.users.service;

import com.trivago.monotony.users.ProductDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class DummyProductService implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(DummyProductService.class);

    @Override
    public Mono<ProductDetails> findProductById(String productId) {
        logger.info("SERVICE: Finding product {}", productId);
        // Simulate latency and potential errors/timeouts per product
        return Mono.delay(Duration.ofMillis(100 + (long) (Math.random() * 200)))
                .flatMap(d -> {
                    if ("product3".equals(productId)) {
                        // Simulate a specific product fetch error
                        return Mono.error(new RuntimeException("Product DB unavailable for " + productId));
                    } else if ("product4".equals(productId)) {
                        // Simulate a timeout for a specific product
                        return Mono.delay(Duration.ofSeconds(2))
                                .then(Mono.just(new ProductDetails(productId, "Product " + productId, "Description...", 40.0)));
                    } else {
                        return Mono.just(new ProductDetails(productId, "Product " + productId, "Description...", Double.parseDouble(productId.substring(7)) * 10.0));
                    }
                });
    }
}
