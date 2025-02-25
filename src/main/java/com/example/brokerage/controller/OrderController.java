package com.example.brokerage.controller;

import com.example.brokerage.entities.Order;
import com.example.brokerage.exceptions.AssetNotFoundException;
import com.example.brokerage.exceptions.InsufficientSharesException;
import com.example.brokerage.exceptions.InvalidOrderStatusException;
import com.example.brokerage.exceptions.OrderNotFoundException;
import com.example.brokerage.service.OrderService;
import com.example.brokerage.service.dto.NewOrderRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController extends MyCustomBaseController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@RequestBody NewOrderRequest newOrderRequest) {
        if (isLoggedUserAllowed(newOrderRequest.getCustomerId())) {
            return ResponseEntity.ok(orderService.placeOrder(newOrderRequest));
        }
        throw new AccessDeniedException("User was not allowed to access requested resource");
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(@RequestParam Long customerId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (isLoggedUserAllowed(customerId)) {
            return ResponseEntity.ok(orderService.listOrders(customerId, start, end));
        }
        throw new AccessDeniedException("User was not allowed to access requested resource");
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/match-order/{orderId}")
    public ResponseEntity<Void> matchOrder(@PathVariable int orderId)
            throws OrderNotFoundException, InvalidOrderStatusException, AssetNotFoundException, InsufficientSharesException {

        orderService.matchOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
