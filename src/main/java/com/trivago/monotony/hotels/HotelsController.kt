package com.trivago.monotony.hotels

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = LoggerFactory.getLogger(HotelsController::class.java)

@RestController
@RequestMapping("/hotels")
class HotelsController(private val hotelsRepository: HotelsRepository) {
    @GetMapping
    fun getHotels(): Flux<Hotel> = hotelsRepository.findAll()

    @GetMapping("/{id}")
    fun getHotel(@PathVariable id: Int): Mono<Hotel> {
        logger.info("Fetching hotel with id {}", id)
        return hotelsRepository.findById(id)
            .switchIfEmpty(Mono.error(HotelNotFoundException(id)))
            .flatMap { hotel ->
                Mono.zip(
                    fetchPrices(hotel.id).collectList(),
                    fetchReviews(hotel.id).collectList(),
                ) { prices, reviews ->
                    Hotel(hotel.id, hotel.name, hotel.description, hotel.rating, reviews, prices)
                }
            }
    }

    private fun fetchPrices(hotelId: Int): Flux<Price> {
        logger.info("Fetching prices for hotel with id {}", hotelId)
        return Flux.just(Price(100, "Expedia"), Price(150, "Booking.com"))
            .delaySequence(Duration.ofSeconds(2))
    }

    private fun fetchReviews(hotelId: Int): Flux<Review> {
        logger.info("Fetching reviews for hotel with id {}", hotelId)
        return Flux.just(Review(4, "It was ok"), Review(5, "It was great"))
            .delaySequence(Duration.ofSeconds(2))
    }
}
