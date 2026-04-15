package com.nutrivista.dto.recipe;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeDetailVO {
    private String                 id;
    private String                 name;
    private String                 nameEn;
    private String                 cuisine;
    private String                 category;
    private String                 subCategory;
    private Integer                calories;
    private Integer                prepTime;
    private Integer                cookTime;
    private Integer                servings;
    private String                 difficulty;
    private String                 description;
    private String                 tips;
    private List<String>           tags;
    private List<RecipeIngredient> ingredients;
    private List<String>           steps;
    private NutritionSummary       nutrition;
    private List<String>           suitableFor;
}
