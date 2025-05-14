package com.trivago.monotony.products;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductsController {
    
    private final ProductsRepository productsRepository;

    public ProductsController(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @GetMapping("/{id}")
    Mono<ProductDetails> getProduct(@PathVariable int id) {
        return findByProductId(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .flatMap(product -> {
                    var recommendationsMono = findRecommendations(product.categoryId())
                            .collectList();

                    return recommendationsMono.zipWith(
                            Mono.just(product),
                            (recommendationsList, prod) -> new ProductDetails(prod, recommendationsList)
                    );
                });
    }

    private Mono<Product> findByProductId(int id) {
        return productsRepository.findById(id);
    }

    private Flux<Product> findRecommendations(int categoryId) {
        return productsRepository.findAllByCategoryId(categoryId);
    }
}
