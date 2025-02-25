package com.example.brokerage.controller;

import com.example.brokerage.config.AuthService;
import com.example.brokerage.config.jwt.JwtTokenProvider;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.service.dto.NewCustomerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtUtil;

    private Customer customer;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        customer = new Customer();
        customer.setUsername("john");
        customer.setPassword("password123");

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("password123")
                .authorities(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    public void testLogin() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(customer, customer.getPassword(), new ArrayList<>()));

        when(authService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(any())).thenReturn("dummy-jwt-token");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"john\", \"password\":\"password123\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("dummy-jwt-token"))
                .andDo(print());
    }

    @Test
    public void testRegister() throws Exception {
        when(authService.saveNewCustomer(any(NewCustomerRequest.class))).thenReturn(customer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"username\":\"john\", \"password\":\"secretPass\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.password").value("password123"))
                .andDo(print());
    }
}
