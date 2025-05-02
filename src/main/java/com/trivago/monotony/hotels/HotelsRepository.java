package com.trivago.monotony.hotels;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HotelsRepository {
    Flux<Hotel> findAll();

    Mono<Hotel> findById(int id);
}
