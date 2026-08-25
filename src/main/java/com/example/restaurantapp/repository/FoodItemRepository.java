package com.example.restaurantapp.repository;

import com.example.restaurantapp.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    
    // Custom query: find foods by category id
    List<FoodItem> findByCategoryId(Long categoryId);
}