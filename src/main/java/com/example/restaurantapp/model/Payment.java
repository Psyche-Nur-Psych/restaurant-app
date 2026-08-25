package com.example.restaurantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    // Payment method: CASH, BKASH, NAGAD, CARD
    private String paymentMethod;

    private String transactionId;

    private LocalDateTime paymentDate;

    // Payment status: PENDING, COMPLETED, FAILED
    private String status;
}