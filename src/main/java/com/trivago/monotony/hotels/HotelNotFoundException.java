package com.trivago.monotony.hotels;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(int id) {
        super("Hotel with id: " + id + " not found");
    }
}
