package com.example.restaurantapp.controller;

import com.example.restaurantapp.model.Category;
import com.example.restaurantapp.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "categories";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Category name cannot be empty!");
            return "redirect:/categories";
        }
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            model.addAttribute("category", categoryOpt.get());
            model.addAttribute("categories", categoryRepository.findAll());
            return "categories";
        }
        redirectAttributes.addFlashAttribute("error", "Category not found!");
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete category as it has associated menu items.");
        }
        return "redirect:/categories";
    }
}
