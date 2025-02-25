package com.example.brokerage.controller;

import com.example.brokerage.entities.Customer;
import com.example.brokerage.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Consumer;
import java.util.function.Function;

public class MyCustomBaseController {

    public Customer getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Customer) auth.getPrincipal();
    }

    public boolean isLoggedUserAllowed(Long customerId) {
        var user = getCurrentUser();
        return user.getId().equals(customerId) || user.getUserRole() == UserRole.ADMIN;
    }

    public <T> T executeIfLoggedUserAllowed(Long customerId, Function<Long, T> stringTFunction) {
        if (isLoggedUserAllowed(customerId)) {
            return stringTFunction.apply(customerId);
        }
        throw new AccessDeniedException("User was not allowed to access requested resource");
    }


    public <T> T executeIfLoggedUserAllowed(Long customerId, Consumer<Long> consumer) {
        if (isLoggedUserAllowed(customerId)) {
            consumer.accept(customerId);
        }
        throw new AccessDeniedException("User was not allowed to access requested resource");
    }
}
