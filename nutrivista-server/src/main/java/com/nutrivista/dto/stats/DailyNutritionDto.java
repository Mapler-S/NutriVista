package com.nutrivista.dto.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 单天营养摄入汇总（宏量 + 关键微量） */
@Getter
@Setter
@NoArgsConstructor
public class DailyNutritionDto {

    private LocalDate date;

    // 宏量营养素
    private BigDecimal energyKcal   = BigDecimal.ZERO;
    private BigDecimal protein       = BigDecimal.ZERO;
    private BigDecimal fat           = BigDecimal.ZERO;
    private BigDecimal carbohydrate  = BigDecimal.ZERO;
    private BigDecimal fiber         = BigDecimal.ZERO;

    // 关键微量营养素（用于充足度分析）
    private BigDecimal vitaminC  = BigDecimal.ZERO;
    private BigDecimal calcium   = BigDecimal.ZERO;
    private BigDecimal iron      = BigDecimal.ZERO;
    private BigDecimal zinc      = BigDecimal.ZERO;
    private BigDecimal vitaminA  = BigDecimal.ZERO;
    private BigDecimal vitaminB1 = BigDecimal.ZERO;
    private BigDecimal vitaminB2 = BigDecimal.ZERO;

    public DailyNutritionDto(LocalDate date) {
        this.date = date;
    }
}
