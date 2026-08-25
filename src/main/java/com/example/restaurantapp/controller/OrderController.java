package com.example.restaurantapp.controller;

import com.example.restaurantapp.model.CustomerOrder;
import com.example.restaurantapp.model.FoodItem;
import com.example.restaurantapp.model.Payment;
import com.example.restaurantapp.repository.CategoryRepository;
import com.example.restaurantapp.repository.FoodItemRepository;
import com.example.restaurantapp.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;

    public static final List<String> STANDARD_TABLES = Arrays.asList(
            "Table 01",
            "Table 02",
            "Table 03",
            "Table 04",
            "Rooftop 01",
            "Rooftop 02",
            "VIP Family Room"
    );

    public OrderController(OrderRepository orderRepository, FoodItemRepository foodItemRepository, CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
        this.categoryRepository = categoryRepository;
    }

    // 1. Render Modern Food Ordering POS Page with Table Collision Status
    @GetMapping("/new")
    public String showOrderForm(Model model) {
        model.addAttribute("foodItems", foodItemRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());

        // Find all currently occupied table numbers (Active Dine-in orders where payment is not yet COMPLETED)
        List<String> occupiedTables = orderRepository.findAll().stream()
                .filter(o -> "Dine-in".equalsIgnoreCase(o.getOrderType()))
                .filter(o -> o.getTableNumber() != null && !o.getTableNumber().isBlank())
                .filter(o -> o.getPayment() == null || !"COMPLETED".equalsIgnoreCase(o.getPayment().getStatus()))
                .map(CustomerOrder::getTableNumber)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("tables", STANDARD_TABLES);
        model.addAttribute("occupiedTables", occupiedTables);
        return "order-form";
    }

    // 2. Process Order Creation (Home Delivery vs Dine-in with Collision Prevention)
    @PostMapping("/create")
    public String createOrder(@RequestParam("customerName") String customerName,
                              @RequestParam(value = "customerPhone", required = false) String customerPhone,
                              @RequestParam(value = "orderType", defaultValue = "Dine-in") String orderType,
                              @RequestParam(value = "tableNumber", required = false) String tableNumber,
                              @RequestParam(value = "deliveryZone", required = false) String deliveryZone,
                              @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
                              @RequestParam(value = "promoCode", required = false) String promoCode,
                              @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
                              @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                              RedirectAttributes redirectAttributes) {

        if (itemIds == null || itemIds.isEmpty() || quantities == null || quantities.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one dish for your order!");
            return "redirect:/orders/new";
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(customerName);
        order.setCustomerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : "N/A");
        order.setOrderType(orderType);
        order.setOrderTime(LocalDateTime.now());

        if ("Delivery".equalsIgnoreCase(orderType)) {
            order.setDeliveryZone((deliveryZone != null && !deliveryZone.isBlank()) ? deliveryZone : "Dhanmondi");
            order.setDeliveryAddress((deliveryAddress != null && !deliveryAddress.isBlank()) ? deliveryAddress : "Home Delivery");
            order.setTableNumber(null); // No table number needed for Home Delivery!
            
            // Delivery Fee based on Dhaka Zone
            double fee = 80.0;
            if ("Dhanmondi".equalsIgnoreCase(deliveryZone)) fee = 60.0;
            else if ("Gulshan".equalsIgnoreCase(deliveryZone) || "Banani".equalsIgnoreCase(deliveryZone)) fee = 90.0;
            else if ("Uttara".equalsIgnoreCase(deliveryZone)) fee = 110.0;
            else if ("Mirpur".equalsIgnoreCase(deliveryZone)) fee = 80.0;
            else if ("Old Dhaka".equalsIgnoreCase(deliveryZone)) fee = 70.0;
            order.setDeliveryFee(fee);
        } else if ("Takeaway".equalsIgnoreCase(orderType)) {
            order.setDeliveryZone("N/A (Takeaway)");
            order.setDeliveryAddress("Counter Pickup");
            order.setTableNumber("Takeaway Counter");
            order.setDeliveryFee(0.0);
        } else {
            // Dine-in Table Service with Table Collision Prevention
            String reqTable = (tableNumber != null && !tableNumber.isBlank()) ? tableNumber : "Table 01";

            // Collision Check: Verify if table is currently occupied by an active unpaid order
            boolean isOccupied = orderRepository.findAll().stream()
                    .filter(o -> "Dine-in".equalsIgnoreCase(o.getOrderType()))
                    .filter(o -> reqTable.equalsIgnoreCase(o.getTableNumber()))
                    .anyMatch(o -> o.getPayment() == null || !"COMPLETED".equalsIgnoreCase(o.getPayment().getStatus()));

            if (isOccupied) {
                redirectAttributes.addFlashAttribute("error", "⚠️ " + reqTable + " is currently OCCUPIED by an active order! Please select another available table.");
                return "redirect:/orders/new";
            }

            order.setDeliveryZone("N/A (Dine-in)");
            order.setDeliveryAddress(null);
            order.setTableNumber(reqTable);
            order.setDeliveryFee(0.0);
        }

        List<FoodItem> selectedFoodItems = new ArrayList<>();
        double subtotal = 0.0;

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            int qty = (i < quantities.size()) ? quantities.get(i) : 0;

            if (qty > 0) {
                Optional<FoodItem> foodOpt = foodItemRepository.findById(itemId);
                if (foodOpt.isPresent()) {
                    FoodItem food = foodOpt.get();
                    subtotal += food.getPrice() * qty;
                    for (int q = 0; q < qty; q++) {
                        selectedFoodItems.add(food);
                    }
                }
            }
        }

        if (selectedFoodItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select valid item quantities greater than 0.");
            return "redirect:/orders/new";
        }

        order.setFoodItems(selectedFoodItems);
        order.setSubtotalAmount(Math.round(subtotal * 100.0) / 100.0);

        // 5% Govt VAT
        double vat = Math.round(subtotal * 0.05 * 100.0) / 100.0;
        order.setVatAmount(vat);

        // Promo Vouchers
        double discount = 0.0;
        if (promoCode != null && !promoCode.isBlank()) {
            String code = promoCode.trim().toUpperCase();
            order.setPromoCode(code);
            if ("KACCHI50".equals(code)) {
                discount = 50.0;
            } else if ("BKASH10".equals(code)) {
                discount = Math.round(subtotal * 0.10 * 100.0) / 100.0;
            } else if ("EIDSPECIAL".equals(code)) {
                discount = 100.0;
            }
        }
        order.setDiscountAmount(discount);

        double grandTotal = Math.max(0.0, Math.round((subtotal + order.getDeliveryFee() + vat - discount) * 100.0) / 100.0);
        order.setTotalAmount(grandTotal);

        // Initialize pending payment
        Payment payment = new Payment();
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("PENDING");
        payment.setPaymentMethod("UNPAID");
        payment.setPaymentDate(LocalDateTime.now());
        order.setPayment(payment);

        CustomerOrder savedOrder = orderRepository.save(order);
        redirectAttributes.addFlashAttribute("success", "Order #" + savedOrder.getId() + " placed successfully for " + (order.getTableNumber() != null ? order.getTableNumber() : "Delivery") + "!");
        return "redirect:/orders/" + savedOrder.getId() + "/checkout";
    }

    // 3. View All Orders List
    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAllByOrderByOrderTimeDesc());
        return "orders";
    }

    // 4. View Order Receipt / Memo Details
    @GetMapping("/{id}")
    public String viewOrderReceipt(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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
            return "receipt";
        }
        redirectAttributes.addFlashAttribute("error", "Order not found!");
        return "redirect:/orders";
    }
}
