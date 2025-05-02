package com.trivago.monotony.users.service;

import com.trivago.monotony.users.Order;
import reactor.core.publisher.Flux;

public interface OrderService {
    Flux<Order> findRecentOrdersByUserId(String userId);
}
