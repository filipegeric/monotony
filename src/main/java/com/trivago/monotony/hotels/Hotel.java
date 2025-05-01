package com.trivago.monotony.hotels;

import java.util.List;

record Hotel(
        int id,
        String name,
        String description,
        int rating,
        List<Review> reviews,
        List<Price> prices
) {
}
