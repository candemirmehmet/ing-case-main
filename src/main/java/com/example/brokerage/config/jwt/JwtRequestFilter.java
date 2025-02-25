package com.example.brokerage.config.jwt;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.example.brokerage.exceptions.CustomerNotFoundException;
import com.example.brokerage.repository.CustomerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    final JwtTokenProvider jwtTokenProvider;
    final CustomerRepository customerRepository;

    public JwtRequestFilter(JwtTokenProvider jwtTokenProvider, CustomerRepository customerRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token != null) {
            var login = jwtTokenProvider.validateToken(token);
            var userDetails = customerRepository.findByUsername(login);
            if (userDetails.isEmpty()) {
                throw new CustomerNotFoundException("A non existing customer is trying to login");
            }
            var authentication = new UsernamePasswordAuthenticationToken(userDetails.get(), userDetails.get().getPassword(), userDetails.get().getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null)
            return null;
        if (!authHeader.startsWith("Bearer ")) {
            throw new JWTDecodeException("Invalid JWT token, Token must start with 'Bearer '");
        }
        return authHeader.replace("Bearer ", "");
    }
}
