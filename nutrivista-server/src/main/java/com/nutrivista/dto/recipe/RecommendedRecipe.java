package com.nutrivista.dto.recipe;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendedRecipe {
    private String              recipeId;
    private String              name;
    private String              cuisine;
    private String              category;
    private Integer             calories;
    private Integer             cookTime;
    private Integer             servings;   // 菜谱原始份数，供前端换算每份重量
    private String              difficulty;
    /** AI 给出的推荐理由 */
    private String              matchReason;
    /** AI 营养点评 */
    private String              nutritionComment;
    /** Milvus 向量相似度分数 */
    private Double              relevanceScore;
    private List<RecipeIngredient> ingredients;
    private List<String>        steps;
    private NutritionSummary    nutrition;
}
