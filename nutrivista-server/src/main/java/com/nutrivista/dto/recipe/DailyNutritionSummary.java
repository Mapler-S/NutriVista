package com.nutrivista.dto.recipe;

import lombok.Data;

import java.math.BigDecimal;

/** 当日已摄入营养素摘要，用于 RAG prompt 中的营养互补分析。 */
@Data
public class DailyNutritionSummary {
    private BigDecimal energyKcal;
    private BigDecimal proteinG;
    private BigDecimal fatG;
    private BigDecimal carbG;
    private BigDecimal fiberG;
}
