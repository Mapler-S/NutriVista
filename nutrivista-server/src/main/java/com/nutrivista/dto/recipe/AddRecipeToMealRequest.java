package com.nutrivista.dto.recipe;

import com.nutrivista.common.constant.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddRecipeToMealRequest {
    @NotNull
    private LocalDate mealDate;
    @NotNull
    private MealType  mealType;
    /** 份数，默认 1 */
    @Min(1)
    private Integer servings = 1;
}
