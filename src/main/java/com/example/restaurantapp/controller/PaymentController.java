package com.example.restaurantapp.controller;

import com.example.restaurantapp.model.CustomerOrder;
import com.example.restaurantapp.model.FoodItem;
import com.example.restaurantapp.model.Payment;
import com.example.restaurantapp.repository.OrderRepository;
import com.example.restaurantapp.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class PaymentController {

    private final OrderRepository orderRepository;

    public PaymentController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
    }

    // Render MFS / Card Checkout View
    @GetMapping("/{id}/checkout")
    public String showCheckoutPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CustomerOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            CustomerOrder order = orderOpt.get();
            model.addAttribute("order", order);

            Map<FoodItem, Integer> itemQuantities = new LinkedHashMap<>();
            if (order.getFoodItems() != null) {
                for (FoodItem item : order.getFoodItems()) {
                    itemQuantities.put(item, itemQuantities.getOrDefault(item, 0) + 1);
                }
            }
            model.addAttribute("itemQuantities", itemQuantities);
            return "checkout";
        }
        redirectAttributes.addFlashAttribute("error", "Order not found!");
        return "redirect:/orders";
    }

    // Process bKash / Nagad / Rocket / Cash Payment
    @PostMapping("/{id}/pay")
    public String processPayment(@PathVariable Long id,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "transactionId", required = false) String transactionId,
                                 @RequestParam(value = "mfsNumber", required = false) String mfsNumber,
                                 RedirectAttributes redirectAttributes) {

        Optional<CustomerOrder> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            CustomerOrder order = orderOpt.get();
            Payment payment = order.getPayment();
            if (payment == null) {
                payment = new Payment();
            }

            if (transactionId == null || transactionId.trim().isEmpty()) {
                String prefix = "TRX-BD-";
                if ("BKASH".equalsIgnoreCase(paymentMethod)) prefix = "BKASH-TRX-";
                else if ("NAGAD".equalsIgnoreCase(paymentMethod)) prefix = "NAGAD-TRX-";
                else if ("ROCKET".equalsIgnoreCase(paymentMethod)) prefix = "ROCKET-TRX-";
                else if ("CARD".equalsIgnoreCase(paymentMethod)) prefix = "CARD-VISA-";
                else if ("CASH".equalsIgnoreCase(paymentMethod)) prefix = "CASH-COUNTER-";
                transactionId = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }

            if (mfsNumber != null && !mfsNumber.isBlank()) {
                transactionId = transactionId + " (" + mfsNumber.trim() + ")";
            }

            payment.setAmount(order.getTotalAmount());
            payment.setPaymentMethod(paymentMethod.toUpperCase());
            payment.setTransactionId(transactionId);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("COMPLETED");

            order.setPayment(payment);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("success", "Payment of ৳" + String.format("%.2f", order.getTotalAmount()) + " via " + paymentMethod + " completed successfully!");
            return "redirect:/orders/" + id;
        }

        redirectAttributes.addFlashAttribute("error", "Order not found!");
        return "redirect:/orders";
    }
}
