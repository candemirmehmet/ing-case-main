package com.example.brokerage.service.dto;

import com.example.brokerage.enums.OrderSide;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NewOrderRequest {
    private Long customerId;
    private String assetName;
    private OrderSide side;
    private int size;
    private BigDecimal price;
}
