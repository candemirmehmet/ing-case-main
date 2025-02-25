package com.example.brokerage.service;

import com.example.brokerage.config.AuthService;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.enums.UserRole;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.service.dto.NewCustomerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    @Test
    void registerCustomer_Success() {
        NewCustomerRequest request = new NewCustomerRequest("newuser", "pass", UserRole.CUSTOMER);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = authService.saveNewCustomer(request);

        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPass", result.getPassword());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.loadUserByUsername("unknown"));
    }

}
