package com.nutrivista.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 菜谱完整信息，对应 MySQL 表 {@code recipe}。
 * tags / ingredients / steps / nutrition_summary / suitable_for 以 JSON 字符串存储。
 */
@Entity
@Table(name = "recipe", indexes = {
        @Index(name = "idx_cuisine",   columnList = "cuisine"),
        @Index(name = "idx_category",  columnList = "category"),
        @Index(name = "idx_calories",  columnList = "calories_per_serving")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "name_en", length = 256)
    private String nameEn;

    @Column(name = "cuisine", length = 32)
    private String cuisine;

    @Column(name = "category", length = 32)
    private String category;

    @Column(name = "sub_category", length = 32)
    private String subCategory;

    @Column(name = "difficulty", length = 16)
    private String difficulty;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;

    @Column(name = "servings")
    private Integer servings;

    @Column(name = "calories_per_serving")
    private Integer caloriesPerServing;

    /** JSON 数组，如 ["下饭菜","经典川菜"] */
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    /** JSON 数组，每项含 name/amount/unit/food_id */
    @Column(name = "ingredients", columnDefinition = "JSON")
    private String ingredients;

    /** JSON 数组，做法步骤字符串列表 */
    @Column(name = "steps", columnDefinition = "JSON")
    private String steps;

    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    /** JSON 对象，含 protein_g/fat_g/carb_g/fiber_g */
    @Column(name = "nutrition_summary", columnDefinition = "JSON")
    private String nutritionSummary;

    /** JSON 数组，如 ["午餐","晚餐"] */
    @Column(name = "suitable_for", columnDefinition = "JSON")
    private String suitableFor;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
