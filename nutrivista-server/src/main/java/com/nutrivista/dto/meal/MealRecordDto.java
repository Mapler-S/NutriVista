package com.nutrivista.dto.meal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/** 单次饮食记录（含明细 + 小计营养） */
@Getter
@Setter
public class MealRecordDto {
    private Long id;
    private String mealType;   // enum name as string
    private String notes;
    private List<MealItemDto> items;

    // 小计（仅统计有数据的项，无数据项贡献 0）
    private BigDecimal totalEnergyKcal;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbohydrate;
    private BigDecimal totalFiber;
}
