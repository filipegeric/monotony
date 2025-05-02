package com.trivago.monotony.users;

import com.trivago.monotony.users.service.OrderService;
import com.trivago.monotony.users.service.ProductService;
import com.trivago.monotony.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    private static final Duration USER_SERVICE_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration ORDER_SERVICE_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration PRODUCT_SERVICE_TIMEOUT_PER_ITEM = Duration.ofMillis(800);

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    public UsersController(UserService userService, OrderService orderService, ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/{userId}")
    public Mono<UserProfileResponse> getUserProfile(@PathVariable String userId) {
        logger.info("Received request for user profile: {}", userId);

        Mono<UserDetails> userDetailsMono = userService.findUserById(userId)
                .timeout(USER_SERVICE_TIMEOUT)
                .doOnError(TimeoutException.class, e -> logger.error("Timeout fetching user details for {}", userId, e))
                .doOnError(e -> !(e instanceof TimeoutException), e -> logger.error("Error fetching user details for {}", userId, e))
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "User not found: " + userId)))
                .onErrorMap(e -> !(e instanceof ResponseStatusException),
                        ex -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to retrieve user details", ex));

        Mono<List<EnrichedOrder>> enrichedOrdersMono = orderService.findRecentOrdersByUserId(userId)
                .timeout(ORDER_SERVICE_TIMEOUT)
                .doOnError(TimeoutException.class, e -> logger.error("Timeout fetching order stream for {}", userId, e))
                .filter(order -> {
                    boolean isCompleted = order.status() == OrderStatus.COMPLETED;
                    if (!isCompleted) {
                        logger.debug("CONTROLLER: Filtering out non-completed order {} for user {}", order.orderId(), userId);
                    }
                    return isCompleted;
                })
                .flatMap(order -> {
                    logger.debug("CONTROLLER: Processing order {} for user {}", order.orderId(), userId);
                    return productService.findProductById(order.primaryProductId())
                            .timeout(PRODUCT_SERVICE_TIMEOUT_PER_ITEM)
                            .map(productDetails -> EnrichedOrder.fromOrderAndProduct(order, productDetails))
                            .doOnSuccess(eo -> logger.debug("Successfully enriched order {}", order.orderId()))
                            .onErrorResume(e -> {
                                if (e instanceof TimeoutException) {
                                    logger.warn("Timeout fetching product {} for order {}. Returning order with error.", order.primaryProductId(), order.orderId());
                                    return Mono.just(EnrichedOrder.fromOrderWithError(order, "Product details timed out"));
                                } else {
                                    logger.warn("Error fetching product {} for order {}: {}. Returning order with error.", order.primaryProductId(), order.orderId(), e.getMessage());
                                    return Mono.just(EnrichedOrder.fromOrderWithError(order, "Product details unavailable: " + e.getMessage()));
                                }
                            });
                }, 5)
                .collectList()
                .onErrorResume(e -> {
                    logger.error("Failed to process order stream for user {}: {}", userId, e.getMessage(), e);
                    return Mono.just(List.of());
                });


        return Mono.zip(userDetailsMono, enrichedOrdersMono)
                .doOnSubscribe(s -> logger.info("CONTROLLER: Starting profile aggregation for user {}", userId))
                .map(tuple -> {
                    UserDetails user = tuple.getT1();
                    List<EnrichedOrder> orders = tuple.getT2();
                    int completedCount = orders.size();
                    logger.info("CONTROLLER: Successfully aggregated profile for user {}. Found {} completed orders.", userId, completedCount);
                    return new UserProfileResponse(user, orders, completedCount);
                })
                .doOnError(e -> logger.error("CONTROLLER: Final aggregation failed for user {}: {}", userId, e.getMessage()))
                .doFinally(signalType -> logger.info("CONTROLLER: Profile request processing finished for user {} with signal: {}", userId, signalType));

    }
}
