package com.nutrivista.dto.recipe;

import lombok.Data;

@Data
public class RecipeIngredient {
    private String name;
    private String amount;
    private String unit;
    private Long   foodId;
}
