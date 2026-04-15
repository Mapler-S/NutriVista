package com.nutrivista.dto.recipe;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeRecommendResponse {
    private List<RecommendedRecipe> recipes;
    /** LLM 整体推荐总结 */
    private String                  aiSummary;
    /** 营养搭配建议 */
    private NutritionAdvice         advice;
}
