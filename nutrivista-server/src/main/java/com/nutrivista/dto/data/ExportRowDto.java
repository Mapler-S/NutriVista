package com.nutrivista.dto.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 导出数据行（每 100g 营养值按实际克重换算） */
@Getter
@AllArgsConstructor
public class ExportRowDto {
    private LocalDate  date;
    private String     mealType;     // 餐次中文名（如"早餐"）
    private String     foodName;
    private BigDecimal weightGram;
    private BigDecimal energyKcal;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal fiber;
}
