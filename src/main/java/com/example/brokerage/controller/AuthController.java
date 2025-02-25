package com.example.brokerage.controller;

import com.example.brokerage.config.AuthService;
import com.example.brokerage.config.jwt.JwtTokenProvider;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.service.dto.AuthRequest;
import com.example.brokerage.service.dto.NewCustomerRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        var authentication = authenticationManager.authenticate(usernamePassword);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok(jwtTokenProvider.generateAccessToken((Customer) authentication.getPrincipal()));
    }

    @PostMapping("/register")
    public ResponseEntity<Customer> register(@RequestBody NewCustomerRequest customer) {
        return ResponseEntity.ok(authService.saveNewCustomer(customer));
    }
}
