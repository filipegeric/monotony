package com.trivago.monotony.products;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(int id) {
        super("Product with id " + id + " not found");
    }
}
