package com.trivago.monotony.products

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/products")
class ProductsController(private val productsRepository: ProductsRepository) {
    @GetMapping("/{id}")
    suspend fun getProduct(@PathVariable id: Int): ProductDetails {
        val product = findByProductId(id).awaitSingleOrNull()
        if (product == null) {
            throw ProductNotFoundException(id)
        }
        val recommendations = findRecommendations(product.categoryId).collectList().awaitSingle()
        return ProductDetails(product, recommendations)
    }

    private fun findByProductId(id: Int): Mono<Product> {
        return productsRepository.findById(id)
    }

    private fun findRecommendations(categoryId: Int): Flux<Product> {
        return productsRepository.findAllByCategoryId(categoryId)
    }
}
