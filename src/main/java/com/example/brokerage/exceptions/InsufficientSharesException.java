package com.example.brokerage.exceptions;

public class InsufficientSharesException extends RuntimeException {
    public InsufficientSharesException(String insufficientSharesToSell) {
        super(insufficientSharesToSell);
    }
}
