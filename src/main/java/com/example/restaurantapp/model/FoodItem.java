package com.example.restaurantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double price;

    private String description;

    private String imageUrl;

    private String spiceLevel; // Mild, Medium, Spicy, Naga Spicy

    // Many Food Items belong to One Category (Many-to-One Relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public String getImageUrlOrDefault() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        String lower = (name != null) ? name.toLowerCase() : "";
        if (lower.contains("kacchi") || lower.contains("biryani") || lower.contains("mutton")) {
            return "/images/mutton_kacchi_biryani.png";
        } else if (lower.contains("kala bhuna") || lower.contains("beef")) {
            return "/images/beef_kala_bhuna.png";
        } else if (lower.contains("khichuri") || lower.contains("ilish")) {
            return "/images/khichuri_ilish.png";
        } else if (lower.contains("borhani")) {
            return "/images/borhani.png";
        } else if (lower.contains("singara") || lower.contains("fuchka") || lower.contains("chotpoti")) {
            return "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=500&q=80";
        } else if (lower.contains("doi") || lower.contains("sweet")) {
            return "/images/bogura_doi.png";
        } else if (lower.contains("cha") || lower.contains("tea")) {
            return "/images/masala_cha.png";
        } else if (lower.contains("kebab")) {
            return "/images/mutton_kebab.png";
        }
        return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&q=80";
    }
}