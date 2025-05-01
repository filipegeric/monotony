package com.trivago.monotony.hotels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/hotels")
public class HotelsController {
    private static final Logger logger = LoggerFactory.getLogger(HotelsController.class);

    private final HotelsRepository hotelsRepository;

    HotelsController(HotelsRepository hotelsRepository) {
        this.hotelsRepository = hotelsRepository;
    }

    @GetMapping
    Flux<Hotel> getHotels() {
        return hotelsRepository.findAll();
    }

    @GetMapping("/{id}")
    Mono<Hotel> getHotel(@PathVariable int id) {
        logger.info("Fetching hotel with id {}", id);
        return hotelsRepository.findById(id)
                .switchIfEmpty(hotelNotFoundError())
                .flatMap(this::toHotelWithPricesAndReviews);
    }

    private Mono<Hotel> toHotelWithPricesAndReviews(Hotel hotel) {
        return Mono.zip(
                fetchPrices(hotel.id()).collectList(),
                fetchReviews(hotel.id()).collectList(),
                (prices, reviews) -> new Hotel(hotel.id(), hotel.name(), hotel.description(), hotel.rating(), reviews, prices)
        );
    }

    private Mono<Hotel> hotelNotFoundError() {
        return Mono.error(new ResponseStatusException(NOT_FOUND, "Hotel not found"));
    }

    private Flux<Price> fetchPrices(int hotelId) {
        logger.info("Fetching prices for hotel with id {}", hotelId);
        return Flux.just(new Price(100, "Expedia"), new Price(150, "Booking.com"))
                .delaySequence(Duration.ofSeconds(2));
    }

    private Flux<Review> fetchReviews(int hotelId) {
        logger.info("Fetching reviews for hotel with id {}", hotelId);
        return Flux.just(new Review(4, "It was ok"), new Review(5, "It was great"))
                .delaySequence(Duration.ofSeconds(2));
    }
}
