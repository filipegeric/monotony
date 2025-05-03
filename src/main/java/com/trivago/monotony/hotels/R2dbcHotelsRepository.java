package com.trivago.monotony.hotels;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class R2dbcHotelsRepository implements HotelsRepository {
    private final DatabaseClient db;

    R2dbcHotelsRepository(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Flux<Hotel> findAll() {
        return db.sql("SELECT id, name, description, rating FROM hotel")
                .fetch()
                .all()
                .map(this::hotelOf);
    }

    @Override
    public Mono<Hotel> findById(int id) {
        return db.sql("SELECT id, name, description, rating FROM hotel WHERE id = :id")
                .bind("id", id)
                .fetch()
                .one()
                .map(this::hotelOf);
    }

    private Hotel hotelOf(Map<String, Object> row) {
        return new Hotel(
                (int) row.get("id"),
                (String) row.get("name"),
                (String) row.get("description"),
                (int) row.get("rating"),
                List.of(),
                List.of()
        );
    }
}
