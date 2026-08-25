package com.example.restaurantapp.service;

import com.example.restaurantapp.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RestaurantReportService {

    private final OrderRepository orderRepository;

    public RestaurantReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Task: Calculate total revenue from all orders
    public Double getTotalRevenue() {
        Double total = orderRepository.calculateTotalSales();
        return (total != null) ? round(total) : 0.0;
    }

    // Task: Count total number of orders placed
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    // Task: Calculate average order value
    public Double getAverageOrderValue() {
        long count = getTotalOrderCount();
        if (count == 0) return 0.0;
        return round(getTotalRevenue() / count);
    }

    private Double round(Double val) {
        if (val == null) return 0.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}