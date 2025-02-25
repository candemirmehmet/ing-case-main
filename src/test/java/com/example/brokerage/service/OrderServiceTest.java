package com.example.brokerage.service;

import com.example.brokerage.entities.Asset;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.entities.Order;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.enums.UserRole;
import com.example.brokerage.exceptions.InsufficientFundsException;
import com.example.brokerage.repository.AssetRepository;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.dto.NewOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Asset tryAsset;
    private Asset stockAsset;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setUserRole(UserRole.CUSTOMER);

        tryAsset = Asset.builder()
                .customer(testCustomer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(10000))
                .usableSize(BigDecimal.valueOf(10000))
                .build();

        stockAsset = Asset.builder()
                .assetName("AAPL")
                .size(BigDecimal.valueOf(100))
                .usableSize(BigDecimal.valueOf(100))
                .build();

        pendingOrder = Order.builder()
                .assetName("AAPL")
                .status(OrderStatus.PENDING)
                .customer(testCustomer)
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(100))
                .build();
    }

    // Place Order Tests
    @Test
    void placeBuyOrder_SufficientFunds_ShouldSucceed() {
        NewOrderRequest request = createRequest(OrderSide.BUY, "AAPL", 10, BigDecimal.valueOf(100));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any())).thenReturn(pendingOrder);

        Order result = orderService.placeOrder(request);

        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void placeBuyOrder_InsufficientFunds_ShouldThrow() {
        NewOrderRequest request = createRequest(OrderSide.BUY, "ABC", 1000, BigDecimal.valueOf(1000));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        assertThrows(InsufficientFundsException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeSellOrder_SufficientShares_ShouldSucceed() {
        NewOrderRequest request = createRequest(OrderSide.SELL, "AAPL", 10, BigDecimal.valueOf(100));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any())).thenReturn(pendingOrder);

        Order result = orderService.placeOrder(request);

        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetRepository).save(stockAsset);
    }

    // Delete Order Tests
    @Test
    void deleteOrder_ByOwner_ShouldSucceed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        orderService.deleteOrder(1L, testCustomer);

        assertEquals(OrderStatus.CANCELED, pendingOrder.getStatus());
        verify(assetRepository).save(any());
    }

    @Test
    void deleteOrder_ByAdmin_ShouldSucceed() {
        Customer admin = new Customer();
        admin.setUserRole(UserRole.ADMIN);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        orderService.deleteOrder(1L, admin);

        assertEquals(OrderStatus.CANCELED, pendingOrder.getStatus());
    }

    @Test
    void deleteOrder_UnauthorizedUser_ShouldThrow() {
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThrows(AccessDeniedException.class, () -> orderService.deleteOrder(1L, otherCustomer));
    }

    // Match Order Tests
    @Test
    void matchBuyOrder_ShouldUpdateAssets() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(assetRepository.findByCustomerIdAndAssetName(eq(1L), eq("AAPL"))).thenReturn(Optional.of(stockAsset));
        when(assetRepository.findByCustomerIdAndAssetName(eq(1L), eq("TRY"))).thenReturn(Optional.of(tryAsset));

        orderService.matchOrder(1L);

        assertEquals(OrderStatus.MATCHED, pendingOrder.getStatus());
        verify(assetRepository, times(2)).save(any());
    }

    @Test
    void matchSellOrder_ShouldUpdateAssets() {
        Order sellOrder = createSellOrder();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL")).thenReturn(Optional.of(stockAsset));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        orderService.matchOrder(1L);

        assertEquals(OrderStatus.MATCHED, sellOrder.getStatus());
        verify(assetRepository, times(2)).save(any());
    }

    // Helper methods
    private NewOrderRequest createRequest(OrderSide side, String asset, int size, BigDecimal price) {
        return NewOrderRequest.builder().customerId(1L).assetName(asset).side(side).size(size).price(price).build();
    }

    private Order createSellOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setCustomer(testCustomer);
        order.setOrderSide(OrderSide.SELL);
        order.setSize(10);
        order.setPrice(BigDecimal.valueOf(100));
        order.setAssetName("AAPL");
        return order;
    }
}
