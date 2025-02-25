package com.example.brokerage.controller;

import com.example.brokerage.config.AuthService;
import com.example.brokerage.config.jwt.JwtTokenProvider;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.service.dto.AuthRequest;
import com.example.brokerage.service.dto.NewCustomerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "classpath:application-test.properties")
class AuthControllerWithMocksTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_ShouldAuthenticateAndReturnToken() {
        // Arrange
        AuthRequest authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        Customer customer = new Customer();
        customer.setUsername("testuser");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(any(Customer.class))).thenReturn("test-jwt-token");

        // Act
        ResponseEntity<String> response = authController.login(authRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test-jwt-token", response.getBody());
        verify(securityContext).setAuthentication(authentication);
        verify(jwtTokenProvider).generateAccessToken(customer);
    }

    @Test
    void register_ShouldCreateNewCustomer() {
        // Arrange
        NewCustomerRequest newCustomerRequest = NewCustomerRequest.builder()
                .username("newuser")
                .password("newpassword")
                .build();

        Customer createdCustomer = new Customer();
        createdCustomer.setUsername("newuser");
        createdCustomer.setId(1L);

        when(authService.saveNewCustomer(any(NewCustomerRequest.class))).thenReturn(createdCustomer);

        // Act
        ResponseEntity<Customer> response = authController.register(newCustomerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals(1L, response.getBody().getId());
        verify(authService).saveNewCustomer(newCustomerRequest);
    }

    @Test
    void login_ShouldUseCorrectAuthenticationToken() {
        // Arrange
        AuthRequest authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        Customer customer = new Customer();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(any(Customer.class))).thenReturn("test-jwt-token");

        // Act
        authController.login(authRequest);

        // Assert - verify the correct token is created with username and password
        verify(authenticationManager).authenticate(
                argThat(token ->
                        token.getPrincipal().equals("testuser") &&
                                token.getCredentials().equals("password")
                )
        );
    }

    @Test
    void login_ShouldSetAuthenticationInSecurityContext() {
        // Arrange
        AuthRequest authRequest = AuthRequest.builder()
                .username("testuser")
                .password("password")
                .build();

        Customer customer = new Customer();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(any(Customer.class))).thenReturn("test-jwt-token");

        // Act
        authController.login(authRequest);

        // Assert
        verify(securityContext).setAuthentication(authentication);
    }

    @Test
    void register_ShouldPassCorrectRequestToService() {
        // Arrange
        NewCustomerRequest newCustomerRequest = NewCustomerRequest.builder()
                .username("newuser")
                .password("newpassword")
                .build();

        Customer createdCustomer = new Customer();
        createdCustomer.setUsername("newuser");
        createdCustomer.setId(1L);

        when(authService.saveNewCustomer(any(NewCustomerRequest.class))).thenReturn(createdCustomer);
        // Act
        authController.register(newCustomerRequest);

        // Assert
        verify(authService).saveNewCustomer(
                argThat(request ->
                        request.getUsername().equals("newuser") && request.getPassword().equals("newpassword")
                )
        );
    }

}
