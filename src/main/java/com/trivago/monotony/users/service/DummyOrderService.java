package com.trivago.monotony.users.service;

import com.trivago.monotony.users.Order;
import com.trivago.monotony.users.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class DummyOrderService implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(DummyOrderService.class);

    @Override
    public Flux<Order> findRecentOrdersByUserId(String userId) {
        logger.info("SERVICE: Finding orders for user {}", userId);
        // Simulate finding multiple orders
        return Flux.range(1, 5)
                .delayElements(Duration.ofMillis(50)) // Simulate streaming results
                .map(i -> {
                    OrderStatus status = (i % 2 == 0) ? OrderStatus.COMPLETED : OrderStatus.PENDING;
                    if (userId.equals("user-error") && i == 3) {
                        // Simulate an error during stream processing in the service layer
                        throw new RuntimeException("Simulated order stream error for user " + userId);
                    }
                    return new Order("order" + i, userId, "product" + i, 10.0 * i, status);
                })
                .doOnSubscribe(s -> logger.info("SERVICE: Order subscription for {}", userId))
                .doOnComplete(() -> logger.info("SERVICE: Order stream complete for {}", userId))
                .doOnError(e -> logger.error("SERVICE: Order stream error for {}", userId, e));
        // .timeout(Duration.ofSeconds(2)); // Can add timeout here too
    }
}
