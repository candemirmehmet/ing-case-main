package com.example.brokerage.config;

import com.example.brokerage.entities.Customer;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.service.dto.NewCustomerRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var customer = customerRepository.findByUsername(username);
        if (customer.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return customer.get();
    }

    public Customer saveNewCustomer(NewCustomerRequest newCustomerRequest) {
        var existingCustomer = customerRepository.findByUsername(newCustomerRequest.getUsername());
        if (existingCustomer.isPresent()) {
            throw new UsernameNotFoundException("User exists already");
        }
        String encryptedPassword = passwordEncoder.encode(newCustomerRequest.getPassword());
        Customer newCustomer = new Customer();
        newCustomer.setUsername(newCustomerRequest.getUsername());
        newCustomer.setPassword(encryptedPassword);
        newCustomer.setUserRole(newCustomerRequest.getUserRole());

        return customerRepository.save(newCustomer);
    }

}
