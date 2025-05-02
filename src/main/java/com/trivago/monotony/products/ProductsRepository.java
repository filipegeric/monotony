package com.trivago.monotony.products;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductsRepository extends ReactiveCrudRepository<Product, Integer> {
    Mono<Product> findById(int id);
    Flux<Product> findAllByCategoryId(int categoryId);
}
