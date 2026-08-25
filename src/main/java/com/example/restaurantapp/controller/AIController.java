package com.example.restaurantapp.controller;

import com.example.restaurantapp.repository.FoodItemRepository;
import com.example.restaurantapp.service.RestaurantAIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/ai-assistant")
public class AIController {

    private final RestaurantAIService aiService;
    private final FoodItemRepository foodItemRepository;

    public AIController(RestaurantAIService aiService, FoodItemRepository foodItemRepository) {
        this.aiService = aiService;
        this.foodItemRepository = foodItemRepository;
    }

    @GetMapping
    public String showAIAssistant(Model model) {
        var foods = foodItemRepository.findAll();
        model.addAttribute("dailySpecials", aiService.generateDailySpecials(foods));
        model.addAttribute("comboDeals", aiService.generateComboRecommendations(foods));
        model.addAttribute("reviewSummary", aiService.summarizeReviews(Arrays.asList(
                "Food was super delicious and hot!",
                "Great service, table was served within 10 minutes.",
                "Loved the Kacchi Biryani and Borhani combo!",
                "Clean environment and polite staff."
        )));
        return "ai-assistant";
    }

    @PostMapping("/query")
    @ResponseBody
    public Map<String, String> askAiAssistantJson(@RequestParam("userQuery") String userQuery) {
        var foods = foodItemRepository.findAll();
        String answerHtml = aiService.answerCustomerQuery(userQuery, foods);
        Map<String, String> response = new HashMap<>();
        response.put("answerHtml", answerHtml);
        return response;
    }

    @PostMapping("/generate-description")
    public String generateDescription(@RequestParam("foodName") String foodName, Model model) {
        var foods = foodItemRepository.findAll();
        model.addAttribute("dailySpecials", aiService.generateDailySpecials(foods));
        model.addAttribute("comboDeals", aiService.generateComboRecommendations(foods));
        model.addAttribute("reviewSummary", aiService.summarizeReviews(Arrays.asList(
                "Food was super delicious and hot!",
                "Great service, table was served within 10 minutes."
        )));
        model.addAttribute("customFoodName", foodName);
        model.addAttribute("generatedDescription", aiService.generateFoodDescription(foodName));
        model.addAttribute("suggestedCategory", aiService.suggestCategory(foodName));
        return "ai-assistant";
    }
}
