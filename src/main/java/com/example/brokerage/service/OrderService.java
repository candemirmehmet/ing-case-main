package com.example.brokerage.service;

import com.example.brokerage.entities.Asset;
import com.example.brokerage.entities.Customer;
import com.example.brokerage.entities.Order;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.enums.UserRole;
import com.example.brokerage.exceptions.AssetNotFoundException;
import com.example.brokerage.exceptions.CustomerNotFoundException;
import com.example.brokerage.exceptions.InsufficientFundsException;
import com.example.brokerage.exceptions.InsufficientSharesException;
import com.example.brokerage.exceptions.InvalidOrderStatusException;
import com.example.brokerage.exceptions.OrderNotFoundException;
import com.example.brokerage.repository.AssetRepository;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.dto.NewOrderRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository,
                        AssetRepository assetRepository,
                        CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
        this.customerRepository = customerRepository;
    }

    private static Order placeOrder(Customer customer, String assetName, OrderSide side, int size, BigDecimal price) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setAssetName(assetName);
        order.setOrderSide(side);
        order.setSize(size);
        order.setPrice(price);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        return order;
    }

    @Transactional
    public Order placeOrder(NewOrderRequest newOrderRequest) throws InsufficientFundsException, InsufficientSharesException {
        Long customerId = newOrderRequest.getCustomerId();
        String assetName = newOrderRequest.getAssetName();
        OrderSide side = newOrderRequest.getSide();
        int size = newOrderRequest.getSize();
        BigDecimal price = newOrderRequest.getPrice();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (side == OrderSide.BUY) {
            var optionalAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY");
            if (optionalAsset.isEmpty()) {
                throw new AssetNotFoundException("Customer has not TRY funds  at all.");
            }

            var tryFunds = optionalAsset.get();
            BigDecimal requiredTRY = BigDecimal.valueOf(size).multiply(price);
            if (tryFunds.getUsableSize() == null || tryFunds.getUsableSize().compareTo(requiredTRY) < 0) {
                throw new InsufficientFundsException("Customer has insufficient TRY funds to create order");
            }

            tryFunds.setUsableSize(tryFunds.getUsableSize().subtract(requiredTRY));
            assetRepository.save(tryFunds);
        } else if (side == OrderSide.SELL) {
            Asset assetToSell = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new AssetNotFoundException("Customer has no share at all to sell."));

            if (assetToSell.getUsableSize().compareTo(BigDecimal.valueOf(size)) < 0) {
                throw new InsufficientSharesException("Customer has insufficient shares to sell");
            }

            assetToSell.setSize(assetToSell.getUsableSize().subtract(BigDecimal.valueOf(size)));
            assetToSell.setUsableSize(assetToSell.getUsableSize().subtract(BigDecimal.valueOf(size)));
            assetRepository.save(assetToSell);
        }

        Order order = placeOrder(customer, assetName, side, size, price);
        return orderRepository.save(order);
    }

    public List<Order> listOrders(Long customerId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, start, end);
    }

    @Transactional
    public void deleteOrder(Long orderId, Customer currentCustomer) throws InvalidOrderStatusException, AssetNotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        Customer owningCustomer = order.getCustomer();

        if (!(owningCustomer.getId().equals(currentCustomer.getId()) || currentCustomer.getUserRole() == UserRole.ADMIN)) {
            throw new AccessDeniedException("You can only delete your own orders or you need to have admin role in order to delete.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Only pending Orders can be deleted");
        }

        var tryFunds = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), "TRY")
                .orElseThrow(() -> new AssetNotFoundException("Customer has no TRY funds."));

        var sizeAsBigDecimal = BigDecimal.valueOf(order.getSize());

        if (order.getOrderSide() == OrderSide.BUY) {
            var costOfOrder = order.getPrice().multiply(sizeAsBigDecimal);
            tryFunds.setUsableSize(tryFunds.getUsableSize().subtract(costOfOrder));
            assetRepository.save(tryFunds);
        } else if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found"));

            asset.setUsableSize(asset.getUsableSize().subtract(sizeAsBigDecimal));
            assetRepository.save(asset);
        }
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public void matchOrder(long orderId) throws OrderNotFoundException, InvalidOrderStatusException, AssetNotFoundException, InsufficientSharesException {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Order is not pending and cannot be matched");
        }

        Long customerId = order.getCustomer().getId();
        var assetName = order.getAssetName();
        var side = order.getOrderSide();
        var sizeAsBigDecimal = BigDecimal.valueOf(order.getSize());
        var price = order.getPrice();

        if (side == OrderSide.BUY) {
            // Increase stock asset
            Asset stockAsset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseGet(() -> createNewAsset(assetName, order));
            stockAsset.setSize(stockAsset.getSize().add(sizeAsBigDecimal));
            stockAsset.setUsableSize(stockAsset.getUsableSize().add(sizeAsBigDecimal));
            assetRepository.save(stockAsset);

            // Decrease TRY asset
            var tryFunds = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseThrow(() -> new AssetNotFoundException("Customer has no TRY funds."));
            var amountToDeduct = sizeAsBigDecimal.multiply(price);
            tryFunds.setSize(tryFunds.getSize().subtract(amountToDeduct));
            tryFunds.setUsableSize(tryFunds.getUsableSize().subtract(amountToDeduct));
            assetRepository.save(tryFunds);
        } else if (side == OrderSide.SELL) {
            // Decrease stock asset
            Asset stockAsset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found"));
            if (stockAsset.getSize().subtract(sizeAsBigDecimal).compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientSharesException("Insufficient shares to sell");
            }
            stockAsset.setSize(stockAsset.getSize().subtract(sizeAsBigDecimal));
            stockAsset.setUsableSize(stockAsset.getUsableSize().subtract(sizeAsBigDecimal));
            assetRepository.save(stockAsset);

            // Increase TRY asset
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseGet(() -> createNewAsset(assetName, order));
            BigDecimal amountToAdd = sizeAsBigDecimal.multiply(price);
            tryAsset.setSize(tryAsset.getSize().add(amountToAdd));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(amountToAdd));
            assetRepository.save(tryAsset);
        }

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }

    private Asset createNewAsset(String assetName, Order order) {
        Asset newAsset = new Asset();
        newAsset.setAssetName(assetName);
        newAsset.setCustomer(order.getCustomer());
        newAsset.setSize(BigDecimal.ZERO);
        newAsset.setUsableSize(BigDecimal.ZERO);
        return assetRepository.save(newAsset);
    }
}
