package com.example.brokerage.exceptions;

public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(String assetNotFound) {
        super(assetNotFound);
    }
}
