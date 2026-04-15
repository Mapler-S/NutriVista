package com.nutrivista.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 餐次热量分布（该时段内平均每次该餐次摄入的热量） */
@Getter
@Setter
@AllArgsConstructor
public class MealDistributionDto {
    private String     mealType;   // 枚举名称字符串
    private BigDecimal avgEnergy;  // kcal
    private int        count;      // 记录天数
}
