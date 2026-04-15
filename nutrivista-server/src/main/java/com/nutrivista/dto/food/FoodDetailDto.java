package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

/** 食物详情（含完整营养成分表） */
@Getter
@Setter
public class FoodDetailDto {
    private Long id;
    private String nameZh;
    private String nameEn;
    private String alias;
    private Integer categoryId;
    private String categoryNameZh;
    private String categoryNameEn;
    private String dataSource;
    private NutritionDto nutrition;
}
