package com.nutrivista.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopRecipeUsageDto {
    private String recipeId;
    private String recipeName;
    private String cuisine;
    private int    adoptedCount;
}
