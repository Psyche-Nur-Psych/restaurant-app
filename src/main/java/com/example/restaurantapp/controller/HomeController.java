package com.example.restaurantapp.controller;

import com.example.restaurantapp.model.AppUser;
import com.example.restaurantapp.model.Category;
import com.example.restaurantapp.model.FoodItem;
import com.example.restaurantapp.repository.CategoryRepository;
import com.example.restaurantapp.repository.FoodItemRepository;
import com.example.restaurantapp.repository.UserRepository;
import com.example.restaurantapp.service.RestaurantAIService;
import com.example.restaurantapp.service.RestaurantReportService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantReportService reportService;
    private final RestaurantAIService aiService;

    public HomeController(FoodItemRepository foodItemRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RestaurantReportService reportService,
                          RestaurantAIService aiService) {
        this.foodItemRepository = foodItemRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
        this.aiService = aiService;
    }

    // 0. Login & Register Pages
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    @PostMapping("/save-user")
    public String registerCustomer(@ModelAttribute AppUser user, RedirectAttributes redirectAttributes) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username and Password are required!");
            return "redirect:/register";
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already taken! Please choose another.");
            return "redirect:/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_CUSTOMER");
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Registration successful! You can now log in as a Customer.");
        return "redirect:/login";
    }

    // 1. Home Dashboard & Summary Report
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalRevenue", reportService.getTotalRevenue());
        model.addAttribute("totalOrders", reportService.getTotalOrderCount());
        model.addAttribute("avgOrder", reportService.getAverageOrderValue());
        model.addAttribute("foodCount", foodItemRepository.count());
        return "index";
    }

    // 2. Menu Page with Category Filtering
    @GetMapping("/menu")
    public String viewMenu(@RequestParam(value = "categoryId", required = false) Long categoryId, Model model) {
        List<FoodItem> foodList;
        if (categoryId != null && categoryId > 0) {
            foodList = foodItemRepository.findByCategoryId(categoryId);
        } else {
            foodList = foodItemRepository.findAll();
        }
        model.addAttribute("foodList", foodList);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        return "menu";
    }

    // 3. Add Food Form Page
    @GetMapping("/add-food")
    public String showAddFoodForm(Model model) {
        model.addAttribute("foodItem", new FoodItem());
        model.addAttribute("categories", categoryRepository.findAll());
        return "add-food";
    }

    // Edit Food Item
    @GetMapping("/edit-food/{id}")
    public String editFoodItem(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FoodItem> foodOpt = foodItemRepository.findById(id);
        if (foodOpt.isPresent()) {
            model.addAttribute("foodItem", foodOpt.get());
            model.addAttribute("categories", categoryRepository.findAll());
            return "add-food";
        }
        redirectAttributes.addFlashAttribute("error", "Food item not found!");
        return "redirect:/menu";
    }

    // Delete Food Item
    @GetMapping("/delete-food/{id}")
    public String deleteFoodItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            foodItemRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Dish deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete dish as it is part of existing orders.");
        }
        return "redirect:/menu";
    }

    // 4. Save Food with AI Description & Auto-Category
    @PostMapping("/save-food")
    public String saveFood(@ModelAttribute FoodItem foodItem,
                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                           @RequestParam(value = "useAi", defaultValue = "false") boolean useAi) {
        if (useAi || foodItem.getDescription() == null || foodItem.getDescription().isBlank()) {
            foodItem.setDescription(aiService.generateFoodDescription(foodItem.getName()));
        }

        if (categoryId != null && categoryId > 0) {
            categoryRepository.findById(categoryId).ifPresent(foodItem::setCategory);
        } else if (foodItem.getCategory() == null) {
            String suggestedCategoryName = aiService.suggestCategory(foodItem.getName());
            Category category = categoryRepository.findByNameIgnoreCase(suggestedCategoryName)
                    .orElseGet(() -> categoryRepository.save(new Category(null, suggestedCategoryName, null)));
            foodItem.setCategory(category);
        }

        foodItemRepository.save(foodItem);
        return "redirect:/menu";
    }
}