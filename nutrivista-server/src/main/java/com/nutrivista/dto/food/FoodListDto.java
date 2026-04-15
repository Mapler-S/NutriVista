package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 食物列表卡片数据（轻量） */
@Getter
@Setter
public class FoodListDto {
    private Long id;
    private String nameZh;
    private String nameEn;
    private String alias;
    private Integer categoryId;
    private String categoryNameZh;
    private String dataSource;

    // 核心营养（每100g）
    private BigDecimal energyKcal;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal fiber;
}
