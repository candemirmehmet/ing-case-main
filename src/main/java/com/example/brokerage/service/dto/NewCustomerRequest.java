package com.example.brokerage.service.dto;

import com.example.brokerage.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NewCustomerRequest {
    private String username;
    private String password;
    private UserRole userRole;
}
