package com.nutrivista.dto.meal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 某天的饮食汇总（所有餐次 + 当天合计营养） */
@Getter
@Setter
public class DailySummaryDto {
    private LocalDate date;
    private List<MealRecordDto> meals;   // 仅含有记录的餐次

    // 当天合计
    private BigDecimal totalEnergyKcal;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbohydrate;
    private BigDecimal totalFiber;
}
