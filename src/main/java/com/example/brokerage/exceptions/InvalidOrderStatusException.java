package com.example.brokerage.exceptions;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String s) {
        super(s);
    }
}
