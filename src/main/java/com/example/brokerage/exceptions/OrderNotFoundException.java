package com.example.brokerage.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderNotFound) {
        super(orderNotFound);
    }
}
