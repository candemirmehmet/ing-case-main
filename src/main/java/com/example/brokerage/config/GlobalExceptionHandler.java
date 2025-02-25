package com.example.brokerage.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.brokerage.exceptions.AssetNotFoundException;
import com.example.brokerage.exceptions.CustomerNotFoundException;
import com.example.brokerage.exceptions.InsufficientFundsException;
import com.example.brokerage.exceptions.InsufficientSharesException;
import com.example.brokerage.exceptions.InvalidOrderStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Map<String, List<String>>> handleGeneralExceptions(Exception ex) {
        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorsMap(errors));
    }

    @ExceptionHandler({
            RuntimeException.class,
            CustomerNotFoundException.class,
            AssetNotFoundException.class,
            InsufficientFundsException.class,
            InvalidOrderStatusException.class,
            InsufficientSharesException.class,
    })
    public final ResponseEntity<Map<String, List<String>>> handleRuntimeExceptions(Exception ex) {
        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorsMap(errors));
    }

    @ExceptionHandler({
            JWTVerificationException.class,
            JWTDecodeException.class
    })
    public ResponseEntity<Map<String, List<String>>> handleJwtErrors(RuntimeException ex) {
        List<String> errors = List.of(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorsMap(errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, List<String>>> handleBadCredentialsError(BadCredentialsException ex) {
        List<String> errors = List.of(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorsMap(errors));
    }

    private Map<String, List<String>> errorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

}
