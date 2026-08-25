package com.example.restaurantapp.config;

import com.example.restaurantapp.model.AppUser;
import com.example.restaurantapp.model.Category;
import com.example.restaurantapp.model.CustomerOrder;
import com.example.restaurantapp.model.FoodItem;
import com.example.restaurantapp.model.Payment;
import com.example.restaurantapp.repository.CategoryRepository;
import com.example.restaurantapp.repository.FoodItemRepository;
import com.example.restaurantapp.repository.OrderRepository;
import com.example.restaurantapp.repository.UserRepository;
import com.example.restaurantapp.service.RestaurantAIService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantAIService aiService;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           FoodItemRepository foodItemRepository,
                           OrderRepository orderRepository,
                           PasswordEncoder passwordEncoder,
                           RestaurantAIService aiService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.foodItemRepository = foodItemRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.aiService = aiService;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Default Users
        if (userRepository.count() == 0) {
            AppUser psychAdmin = new AppUser(null, "Psych", passwordEncoder.encode("Psych"), "ROLE_ADMIN");
            AppUser standardAdmin = new AppUser(null, "admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
            AppUser staffUser = new AppUser(null, "staff", passwordEncoder.encode("staff123"), "ROLE_STAFF");
            AppUser customerUser = new AppUser(null, "customer", passwordEncoder.encode("customer123"), "ROLE_CUSTOMER");
            userRepository.saveAll(Arrays.asList(psychAdmin, standardAdmin, staffUser, customerUser));
        }

        // 2. Seed Authentic Bangladeshi Categories
        Category biryaniCat = categoryRepository.findByNameIgnoreCase("Kacchi & Biryani Special")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Kacchi & Biryani Special", null)));
        Category curriesCat = categoryRepository.findByNameIgnoreCase("Traditional Curries & Rice")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Traditional Curries & Rice", null)));
        Category streetFoodCat = categoryRepository.findByNameIgnoreCase("Bangladeshi Street Food")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Bangladeshi Street Food", null)));
        Category sweetsCat = categoryRepository.findByNameIgnoreCase("Bangla Sweets & Desserts")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Bangla Sweets & Desserts", null)));
        Category teaCat = categoryRepository.findByNameIgnoreCase("Beverages & Tea")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Beverages & Tea", null)));

        // 3. Seed Authentic Food Items in ৳ BDT with Custom Uploaded Food Photos
        FoodItem kacchi = new FoodItem(null, "Old Dhaka Shahi Mutton Kacchi Biryani", 450.0, 
                "Authentic mutton kacchi cooked with basmati rice, potatoes, aromatic herbs & pure ghee.", 
                "/images/mutton_kacchi_biryani.png", "Spicy", biryaniCat);

        FoodItem kalaBhuna = new FoodItem(null, "Chittagong Beef Kala Bhuna", 550.0, 
                "Traditional slow-cooked dark beef infused with authentic Chattogram roasted spices.", 
                "/images/beef_kala_bhuna.png", "Naga Spicy", curriesCat);

        FoodItem khichuri = new FoodItem(null, "Bhuna Khichuri with Mustard Ilish", 480.0, 
                "Rich aromatic yellow rice and lentils served with crispy fried Padmar Ilish fish.", 
                "/images/khichuri_ilish.png", "Medium", curriesCat);

        FoodItem borhani = new FoodItem(null, "Special Chittagong Borhani", 120.0, 
                "Traditional refreshing spiced yogurt drink seasoned with mint, coriander, and mustard seeds.", 
                "/images/borhani.png", "Mild", teaCat);

        FoodItem singara = new FoodItem(null, "Dhaka Special Crispy Singara (4 Pcs)", 150.0, 
                "Crispy hot pastry stuffed with spiced potato mash, green peas, peanuts & tangy sauce.", 
                "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=500&q=80", "Spicy", streetFoodCat);

        FoodItem doi = new FoodItem(null, "Bogura Special Original Doi", 180.0, 
                "Authentic clay-pot fresh original yogurt caramelized to thick creaminess.", 
                "/images/bogura_doi.png", "Mild", sweetsCat);

        FoodItem cha = new FoodItem(null, "Srimangal Masala Milk Cha", 40.0, 
                "Rich Sylheti black tea simmered with condensed milk, cardamom, clove & ginger.", 
                "/images/masala_cha.png", "Mild", teaCat);

        FoodItem kebab = new FoodItem(null, "Shahi Mutton Reshmi Jali Kebab (4 Pcs)", 220.0, 
                "Mutton minced kebab wrapped in crispy egg lattice net, spiced with royale herbs.", 
                "/images/mutton_kebab.png", "Medium", biryaniCat);

        if (foodItemRepository.count() == 0) {
            foodItemRepository.saveAll(Arrays.asList(kacchi, kalaBhuna, khichuri, borhani, singara, doi, cha, kebab));
        } else {
            List<FoodItem> existing = foodItemRepository.findAll();
            for (FoodItem item : existing) {
                String lower = item.getName().toLowerCase();
                if (lower.contains("kacchi") || lower.contains("biryani") || lower.contains("khashir") || lower.contains("mutton")) {
                    item.setName("Old Dhaka Shahi Mutton Kacchi Biryani");
                    item.setImageUrl("/images/mutton_kacchi_biryani.png");
                } else if (lower.contains("kala bhuna") || lower.contains("beef")) {
                    item.setName("Chittagong Beef Kala Bhuna");
                    item.setImageUrl("/images/beef_kala_bhuna.png");
                } else if (lower.contains("khichuri") || lower.contains("ilish")) {
                    item.setName("Bhuna Khichuri with Mustard Ilish");
                    item.setImageUrl("/images/khichuri_ilish.png");
                } else if (lower.contains("borhani")) {
                    item.setName("Special Chittagong Borhani");
                    item.setImageUrl("/images/borhani.png");
                } else if (lower.contains("fuchka") || lower.contains("singara") || lower.contains("chotpoti")) {
                    item.setName("Dhaka Special Crispy Singara (4 Pcs)");
                    item.setDescription("Crispy hot pastry stuffed with spiced potato mash, green peas, peanuts & tangy sauce.");
                    item.setImageUrl("https://images.unsplash.com/photo-1601050690597-df0568f70950?w=500&q=80");
                } else if (lower.contains("doi") || lower.contains("sweet")) {
                    item.setName("Bogura Special Original Doi");
                    item.setDescription("Authentic clay-pot fresh original yogurt caramelized to thick creaminess.");
                    item.setImageUrl("/images/bogura_doi.png");
                } else if (lower.contains("cha") || lower.contains("tea")) {
                    item.setName("Srimangal Masala Milk Cha");
                    item.setImageUrl("/images/masala_cha.png");
                } else if (lower.contains("kebab")) {
                    item.setName("Shahi Mutton Reshmi Jali Kebab (4 Pcs)");
                    item.setImageUrl("/images/mutton_kebab.png");
                }
                foodItemRepository.save(item);
            }
        }

        // 4. Seed Initial Completed Executive Sales Orders (Cash, bKash, Nagad, Card)
        if (orderRepository.count() == 0 && kacchi != null) {
            
            // Order 1: Tanvir Hossain (bKash Delivery)
            CustomerOrder o1 = new CustomerOrder();
            o1.setCustomerName("Tanvir Hossain");
            o1.setCustomerPhone("01711-223344");
            o1.setOrderType("Delivery");
            o1.setDeliveryZone("Dhanmondi");
            o1.setDeliveryAddress("House 42, Road 7A, Dhanmondi, Dhaka");
            o1.setDeliveryFee(60.0);
            o1.setSubtotalAmount(1140.0);
            o1.setVatAmount(57.0);
            o1.setDiscountAmount(50.0);
            o1.setPromoCode("KACCHI50");
            o1.setTotalAmount(1207.0);
            o1.setOrderTime(LocalDateTime.now().minusHours(6));
            o1.setFoodItems(Arrays.asList(kacchi, kacchi, borhani, borhani));

            Payment p1 = new Payment(null, 1207.0, "BKASH", "BKASH-TRX-9A8811", LocalDateTime.now().minusHours(6), "COMPLETED");
            o1.setPayment(p1);

            // Order 2: Nusrat Jahan (Nagad Delivery)
            CustomerOrder o2 = new CustomerOrder();
            o2.setCustomerName("Nusrat Jahan");
            o2.setCustomerPhone("01822-334455");
            o2.setOrderType("Delivery");
            o2.setDeliveryZone("Gulshan");
            o2.setDeliveryAddress("Plot 15, Road 113, Gulshan-2, Dhaka");
            o2.setDeliveryFee(90.0);
            o2.setSubtotalAmount(2300.0);
            o2.setVatAmount(115.0);
            o2.setDiscountAmount(230.0);
            o2.setPromoCode("BKASH10");
            o2.setTotalAmount(2275.0);
            o2.setOrderTime(LocalDateTime.now().minusHours(5));
            o2.setFoodItems(Arrays.asList(kalaBhuna, kalaBhuna, khichuri, khichuri, borhani, borhani));

            Payment p2 = new Payment(null, 2275.0, "NAGAD", "NAGAD-TRX-776655", LocalDateTime.now().minusHours(5), "COMPLETED");
            o2.setPayment(p2);

            // Order 3: Anik Chowdhury (Cash on Delivery)
            CustomerOrder o3 = new CustomerOrder();
            o3.setCustomerName("Anik Chowdhury");
            o3.setCustomerPhone("01744-556677");
            o3.setOrderType("Delivery");
            o3.setDeliveryZone("Mirpur");
            o3.setDeliveryAddress("Block D, Road 4, Mirpur-10, Dhaka");
            o3.setDeliveryFee(80.0);
            o3.setSubtotalAmount(2000.0);
            o3.setVatAmount(100.0);
            o3.setDiscountAmount(50.0);
            o3.setPromoCode("KACCHI50");
            o3.setTotalAmount(2130.0);
            o3.setOrderTime(LocalDateTime.now().minusHours(4));
            o3.setFoodItems(Arrays.asList(kacchi, kacchi, kalaBhuna, borhani, borhani, doi));

            Payment p3 = new Payment(null, 2130.0, "CASH", "CASH-ON-DELIVERY-101", LocalDateTime.now().minusHours(4), "COMPLETED");
            o3.setPayment(p3);

            // Order 4: Farhana Yeasmin (Cash at Counter / Dine-in)
            CustomerOrder o4 = new CustomerOrder();
            o4.setCustomerName("Farhana Yeasmin");
            o4.setCustomerPhone("01555-667788");
            o4.setOrderType("Dine-in");
            o4.setTableNumber("Table 04");
            o4.setDeliveryFee(0.0);
            o4.setSubtotalAmount(1220.0);
            o4.setVatAmount(61.0);
            o4.setDiscountAmount(0.0);
            o4.setTotalAmount(1281.0);
            o4.setOrderTime(LocalDateTime.now().minusHours(3));
            o4.setFoodItems(Arrays.asList(khichuri, khichuri, kebab, borhani, borhani));

            Payment p4 = new Payment(null, 1281.0, "CASH", "CASH-COUNTER-102", LocalDateTime.now().minusHours(3), "COMPLETED");
            o4.setPayment(p4);

            // Order 5: Rafiqul Islam (Dine-in Card)
            CustomerOrder o5 = new CustomerOrder();
            o5.setCustomerName("Rafiqul Islam");
            o5.setCustomerPhone("01933-445566");
            o5.setOrderType("Dine-in");
            o5.setTableNumber("VIP Family Room");
            o5.setDeliveryFee(0.0);
            o5.setSubtotalAmount(3400.0);
            o5.setVatAmount(170.0);
            o5.setDiscountAmount(100.0);
            o5.setPromoCode("EIDSPECIAL");
            o5.setTotalAmount(3470.0);
            o5.setOrderTime(LocalDateTime.now().minusHours(2));
            o5.setFoodItems(Arrays.asList(kebab, kebab, kebab, kebab, kacchi, kacchi, kacchi, kacchi, doi, doi, doi, doi));

            Payment p5 = new Payment(null, 3470.0, "CARD", "CARD-TRX-998877", LocalDateTime.now().minusHours(2), "COMPLETED");
            o5.setPayment(p5);

            // Order 6: Sabrina Ahmed (Rooftop bKash Dine-in)
            CustomerOrder o6 = new CustomerOrder();
            o6.setCustomerName("Sabrina Ahmed");
            o6.setCustomerPhone("01611-998877");
            o6.setOrderType("Dine-in");
            o6.setTableNumber("Rooftop 01");
            o6.setDeliveryFee(0.0);
            o6.setSubtotalAmount(3050.0);
            o6.setVatAmount(152.50);
            o6.setDiscountAmount(50.0);
            o6.setPromoCode("KACCHI50");
            o6.setTotalAmount(3152.50);
            o6.setOrderTime(LocalDateTime.now().minusHours(1));
            o6.setFoodItems(Arrays.asList(kacchi, kacchi, kacchi, kalaBhuna, kalaBhuna, borhani, borhani, borhani, borhani, borhani));

            Payment p6 = new Payment(null, 3152.50, "BKASH", "BKASH-TRX-771122", LocalDateTime.now().minusHours(1), "COMPLETED");
            o6.setPayment(p6);

            orderRepository.saveAll(Arrays.asList(o1, o2, o3, o4, o5, o6));
        }
    }
}
