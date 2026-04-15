package com.nutrivista.dto.recipe;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class RecipeRecommendRequest {
    /** 餐次，如 BREAKFAST / LUNCH / DINNER / SNACK */
    private String mealType;
    /** 用户自然语言描述，如"想吃清淡点的" */
    private String userQuery;
    /** 偏好菜系，如"川菜"（可选） */
    private String preferredCuisine;
    /** 热量上限 kcal（可选） */
    private Integer maxCalories;
    /** 难度偏好：easy / medium / hard（可选） */
    private String difficulty;
    /** 推荐数量，默认 3 */
    @Min(1) @Max(10)
    private Integer count = 3;
    /** 排除食材列表，如 ["花生", "海鲜"]（可选） */
    private List<String> excludeIngredients;
    /** 计划用餐日期，格式 yyyy-MM-dd（可选） */
    private String mealDate;
    /** 当天已有饮食营养摘要，用于营养互补推荐（可选） */
    private DailyNutritionSummary existingIntake;
}
