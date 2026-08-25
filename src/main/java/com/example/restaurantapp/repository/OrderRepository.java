package com.example.restaurantapp.repository;

import com.example.restaurantapp.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByOrderTimeDesc();

    // Calculate the grand total sales of completed orders
    @Query("SELECT SUM(o.totalAmount) FROM CustomerOrder o WHERE o.payment.status = 'COMPLETED'")
    Double calculateTotalSales();

    // Count completed orders
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.payment.status = 'COMPLETED'")
    long countCompletedOrders();
}