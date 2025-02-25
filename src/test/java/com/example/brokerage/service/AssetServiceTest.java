package com.example.brokerage.service;


import com.example.brokerage.entities.Asset;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.enums.UserRole;
import com.example.brokerage.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset tryAsset;
    private Asset stockAsset;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Setup test data
        customer = Customer.builder()
                .username("john")
                .password("password123")
                .userRole(UserRole.CUSTOMER)
                .build();
        tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(10000))
                .usableSize(BigDecimal.valueOf(10000))
                .build();

        stockAsset = Asset.builder()
                .customer(customer)
                .assetName("AAPL")
                .size(BigDecimal.valueOf(20))
                .usableSize(BigDecimal.valueOf(20))
                .build();
    }

    @Test
    void getAssetsByCustomerId_ShouldReturnAssets() {
        // Arrange
        List<Asset> assetList = Arrays.asList(tryAsset, stockAsset);
        when(assetRepository.findByCustomerId(1L)).thenReturn(assetList);

        // Act
        List<Asset> result = assetService.getAssetsByCustomerId(1L);

        // Assert
        assertEquals(assetList, result);
        assertEquals(2, result.size());
    }

    @Test
    void getAssetByCustomerIdAndName_WhenAssetExists_ShouldReturnAsset() {
        // Arrange
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        // Act
        var result = assetService.getAssetByCustomerIdAndName(1L, "TRY");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tryAsset, result.get());
    }

    @Test
    void getAssetByCustomerIdAndName_WhenAssetDoesNotExist_ShouldThrowException() {
        // Arrange
        when(assetRepository.findByCustomerIdAndAssetName(1L, "BTC"))
                .thenReturn(Optional.empty());
        // Act & Assert
        assertTrue(assetService.getAssetByCustomerIdAndName(1L, "BTC").isEmpty());
    }
}
