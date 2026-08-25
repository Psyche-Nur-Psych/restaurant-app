package com.example.restaurantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String customerPhone;

    private String tableNumber;

    private String orderType; // Dine-in, Delivery, Takeaway

    private String deliveryZone; // Dhanmondi, Gulshan, Banani, Uttara, Mirpur, Old Dhaka

    @Column(length = 500)
    private String deliveryAddress;

    private Double deliveryFee = 0.0;

    private Double subtotalAmount = 0.0;

    private Double vatAmount = 0.0;

    private Double discountAmount = 0.0;

    private String promoCode;

    private LocalDateTime orderTime;

    // Many-to-Many: One order can contain multiple food items
    @ManyToMany
    @JoinTable(
        name = "order_food_items",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "food_item_id")
    )
    private List<FoodItem> foodItems = new ArrayList<>();

    // One-to-One: Each order is linked to one payment
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Double totalAmount;
}