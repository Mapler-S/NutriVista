package com.nutrivista.dto.meal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 饮食记录明细（含按实际克重换算后的营养值） */
@Getter
@Setter
public class MealItemDto {
    private Long id;
    private Long mealId;
    private Long foodId;
    private String nameZh;
    private String nameEn;
    private BigDecimal weightGram;
    private Integer sortOrder;

    // 按 weightGram 换算后的营养（可为 null，数据缺失时）
    private BigDecimal energyKcal;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal fiber;
}
