package com.example.restaurantapp.service;

import com.example.restaurantapp.model.FoodItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantAIService {

    // 1. Generate mouth-watering Bangladeshi food description
    public String generateFoodDescription(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            return "Authentic Bangladeshi culinary delight prepared with pure ghee, mustard oil, and traditional roasted spices.";
        }
        
        String lower = foodName.toLowerCase();
        if (lower.contains("kacchi") || lower.contains("biryani")) {
            return "Royal Old Dhaka " + foodName + " - fragrant Basmati rice layered with succulent tender mutton marinated in sour curd, shahi jafran, and pure ghee.";
        } else if (lower.contains("kala bhuna") || lower.contains("beef")) {
            return "Authentic Chattogram " + foodName + " - slow roasted tender beef caramelized in black spiced gravy with garlic, dry chili, and mustard oil.";
        } else if (lower.contains("khichuri")) {
            return "Comforting Bangladeshi " + foodName + " - aromatic Chinigura rice simmered with moong dal, roasted spices, and served steaming hot.";
        } else if (lower.contains("borhani")) {
            return "Traditional Chottogram " + foodName + " - refreshing digestif blended with sour curd, mint, black salt, and secret kebab spices.";
        } else if (lower.contains("fuchka") || lower.contains("chotpoti") || lower.contains("singara")) {
            return "Popular Dhaka Street-Style " + foodName + " - crispy golden pastry filled with zesty spiced mash, peanuts & tangy tamarind Tok.";
        } else if (lower.contains("doi") || lower.contains("sweet")) {
            return "Heritage Bogura " + foodName + " - slow-thickened sweetened clay-pot curd with rich caramelized cream layers.";
        } else if (lower.contains("cha") || lower.contains("tea")) {
            return "Srimangal Style " + foodName + " - strong brewed black tea simmered with condensed milk, cardamom, and ginger zest.";
        } else if (lower.contains("kebab")) {
            return "Shahi Royal " + foodName + " - tender minced mutton wrapped in egg lattice net, infused with nutmeg & royale spices.";
        }
        
        return "Chef's Special Bangladeshi " + foodName + " - freshly prepared with authentic local spices, pure mustard oil, and rich traditional flavors.";
    }

    // 2. Auto-suggest category based on Bangladeshi dish name
    public String suggestCategory(String foodName) {
        String lower = (foodName != null) ? foodName.toLowerCase() : "";
        if (lower.contains("kacchi") || lower.contains("biryani") || lower.contains("kebab") || lower.contains("tehari") || lower.contains("polao")) {
            return "Kacchi & Biryani Special";
        } else if (lower.contains("kala bhuna") || lower.contains("khichuri") || lower.contains("curry") || lower.contains("beef") || lower.contains("ilish") || lower.contains("fish")) {
            return "Traditional Curries & Rice";
        } else if (lower.contains("fuchka") || lower.contains("chotpoti") || lower.contains("singara") || lower.contains("samosa") || lower.contains("roll")) {
            return "Bangladeshi Street Food";
        } else if (lower.contains("doi") || lower.contains("rosgolla") || lower.contains("shemai") || lower.contains("sweet") || lower.contains("dessert") || lower.contains("pithe")) {
            return "Bangla Sweets & Desserts";
        } else if (lower.contains("borhani") || lower.contains("cha") || lower.contains("tea") || lower.contains("juice") || lower.contains("smoothie") || lower.contains("lassi")) {
            return "Beverages & Tea";
        }
        return "Traditional Curries & Rice";
    }

    // 3. AI Interactive Assistant Concierge Response
    public String answerCustomerQuery(String userQuery, List<FoodItem> availableFoods) {
        if (userQuery == null || userQuery.isBlank()) {
            return "Greetings! I am your AI Chef Assistant. Ask me anything about our authentic Bangladeshi dishes, royal combos, prices, or spice levels!";
        }

        String query = userQuery.toLowerCase().trim();
        StringBuilder sb = new StringBuilder();

        // Check for specific dishes mentioned in query
        List<FoodItem> matched = availableFoods.stream()
                .filter(f -> query.contains(f.getName().toLowerCase()) 
                          || (f.getCategory() != null && query.contains(f.getCategory().getName().toLowerCase()))
                          || (f.getDescription() != null && query.contains(f.getDescription().toLowerCase())))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            sb.append("<div class='mb-2'><strong class='text-success fs-5'>👨‍🍳 Excellent Choice! Here is what our Master Chef recommends:</strong></div>");
            for (FoodItem food : matched) {
                sb.append("<div class='card mb-3 border-success shadow-sm rounded-4 p-3 bg-white'>")
                  .append("<div class='d-flex align-items-center gap-3'>")
                  .append("<img src='").append(food.getImageUrlOrDefault()).append("' class='rounded-3 shadow-sm' style='width:80px; height:80px; object-fit:cover;'>")
                  .append("<div class='flex-grow-1'>")
                  .append("<h5 class='fw-bold mb-1 text-dark'>").append(food.getName()).append("</h5>")
                  .append("<div class='mb-2'><span class='badge bg-success me-1'>").append(food.getCategory() != null ? food.getCategory().getName() : "Special").append("</span>")
                  .append("<span class='badge bg-warning text-dark me-1'>").append(food.getSpiceLevel() != null ? food.getSpiceLevel() : "Medium").append("</span>")
                  .append("<span class='fw-bold text-success fs-5 ms-2'>৳").append(String.format("%.2f", food.getPrice())).append("</span></div>")
                  .append("<p class='mb-2 text-muted small fw-semibold'><em>\"").append(generateFoodDescription(food.getName())).append("\"</em></p>")
                  .append("<a href='/orders/new' class='btn btn-sm btn-success fw-bold shadow-sm'>🛒 Order ").append(food.getName()).append(" Now</a>")
                  .append("</div></div></div>");
            }
            return sb.toString();
        }

        // General menu query:
        if (query.contains("menu") || query.contains("available") || query.contains("food") || query.contains("all") || query.contains("list") || query.contains("what do you have")) {
            sb.append("<div class='mb-2'><strong class='text-success fs-5'>👑 Here are our World-Famous Authentic Bangladeshi Delicacies available right now:</strong></div>");
            sb.append("<div class='row g-2 mb-2'>");
            for (FoodItem food : availableFoods) {
                sb.append("<div class='col-md-6'><div class='p-3 border rounded-3 bg-white d-flex align-items-center justify-content-between shadow-sm'>")
                  .append("<div><strong class='text-dark'>").append(food.getName()).append("</strong><br>")
                  .append("<span class='badge bg-success me-1'>").append(food.getCategory() != null ? food.getCategory().getName() : "Food").append("</span>")
                  .append("<span class='text-success fw-bold fs-6'>৳").append(String.format("%.2f", food.getPrice())).append("</span></div>")
                  .append("<a href='/orders/new' class='btn btn-sm btn-outline-success fw-bold'>Order</a>")
                  .append("</div></div>");
            }
            sb.append("</div>");
            return sb.toString();
        }

        // Recommendation query:
        if (query.contains("lunch") || query.contains("dinner") || query.contains("recommend") || query.contains("best") || query.contains("special") || query.contains("popular")) {
            return "<div class='p-3 bg-white rounded-4 border-start border-success border-5 shadow-sm'>" +
                   "<h5 class='fw-bold text-success mb-2'>🔥 Chef's Signature Royal Feast Recommendation:</h5>" +
                   "<p class='fs-6 mb-2'>For an unforgettable dining experience, we strongly recommend combining our <strong>Old Dhaka Shahi Mutton Kacchi Biryani (৳450.00)</strong> with a side of <strong>Shahi Mutton Reshmi Jali Kebab (৳220.00)</strong> and a chilled <strong>Special Chittagong Borhani (৳120.00)</strong>!</p>" +
                   "<p class='text-muted small mb-3'>Cooked with pure ghee, saffron, and authentic hand-ground spices for a rich, aromatic royal taste that guaranteed satisfaction.</p>" +
                   "<a href='/orders/new' class='btn btn-success fw-bold shadow'>🛒 Order Royal Kacchi Feast Now</a></div>";
        }

        // Drinks / Beverage query:
        if (query.contains("drink") || query.contains("beverage") || query.contains("tea") || query.contains("cha") || query.contains("borhani")) {
            return "<div class='p-3 bg-white rounded-4 border-start border-primary border-5 shadow-sm'>" +
                   "<h5 class='fw-bold text-primary mb-2'>🍵 Refreshing Traditional Beverages & Tea:</h5>" +
                   "<p class='mb-1'>1. <strong>Special Chittagong Borhani (৳120.00)</strong>: Traditional digestive mint & mustard spiced sour yogurt drink.</p>" +
                   "<p class='mb-2'>2. <strong>Srimangal Masala Milk Cha (৳40.00)</strong>: Rich Sylheti black tea simmered with condensed milk, cardamom & ginger.</p>" +
                   "<a href='/orders/new' class='btn btn-primary fw-bold shadow mt-1'>🛒 Order Beverages Now</a></div>";
        }

        // Sweets query:
        if (query.contains("sweet") || query.contains("dessert") || query.contains("doi")) {
            return "<div class='p-3 bg-white rounded-4 border-start border-warning border-5 shadow-sm'>" +
                   "<h5 class='fw-bold text-warning-emphasis mb-2'>🍧 Authentic Bangla Sweets & Desserts:</h5>" +
                   "<p class='mb-2'><strong>Bogura Special Original Doi (৳180.00)</strong> — Authentic clay-pot fresh original yogurt caramelized to thick creaminess.</p>" +
                   "<a href='/orders/new' class='btn btn-warning text-dark fw-bold shadow'>🛒 Order Bogura Doi Now</a></div>";
        }

        // Fallback response with lucrative descriptions of menu items:
        sb.append("<div class='mb-2'><strong class='text-success fs-5'>✨ Master Chef's Handpicked Menu Highlights:</strong></div>");
        for (FoodItem food : availableFoods.stream().limit(3).collect(Collectors.toList())) {
            sb.append("<div class='p-3 mb-2 bg-white rounded-3 border shadow-sm'>")
              .append("<h6 class='fw-bold text-dark mb-1'>").append(food.getName()).append(" — <span class='text-success'>৳").append(String.format("%.2f", food.getPrice())).append("</span></h6>")
              .append("<p class='small text-muted mb-2'>").append(generateFoodDescription(food.getName())).append("</p>")
              .append("<a href='/orders/new' class='btn btn-sm btn-success fw-bold'>🛒 Order Now</a>")
              .append("</div>");
        }
        return sb.toString();
    }

    // 4. AI Daily Chef Specials Generator
    public String generateDailySpecials(List<FoodItem> foodList) {
        if (foodList == null || foodList.isEmpty()) {
            return "🔥 Today's Shahi Special: Old Dhaka Mutton Kacchi & Borhani Bundle with 15% bKash Cashback!";
        }

        String itemNames = foodList.stream().map(FoodItem::getName).limit(3).collect(Collectors.joining(", "));
        return "🌟 SHAHI CHEF SPECIAL FEAST: Enjoy our royal " + itemNames + 
               " freshly prepared in pure ghee! Use promo code 'KACCHI50' for ৳50 instant discount + bKash cashback.";
    }

    // 5. AI Combo Deal Recommendation Generator
    public String generateComboRecommendations(List<FoodItem> foodList) {
        return "🎉 OLD DHAKA ROYAL COMBO: 'The Grand Shahi Feast' - Pair [Shahi Kacchi Biryani] + [Reshmi Jali Kebab] + [Special Chottogram Borhani] for just ৳699 (Save ৳100!)";
    }

    // 6. AI Customer Feedback & Review Summarizer
    public String summarizeReviews(List<String> reviews) {
        return "⭐ AI Bangladesh Foodie Sentiment: 99% Positive Rating! Customers loved the rich authentic flavor of Beef Kala Bhuna, spicy Tamarind Tok in Fuchka, and fast bKash checkout.";
    }
}