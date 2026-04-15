package com.nutrivista.dto.recipe;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeListVO {
    private String       id;
    private String       name;
    private String       nameEn;
    private String       cuisine;
    private String       category;
    private Integer      calories;
    private Integer      prepTime;
    private Integer      cookTime;
    private String       difficulty;
    private List<String> tags;
}
