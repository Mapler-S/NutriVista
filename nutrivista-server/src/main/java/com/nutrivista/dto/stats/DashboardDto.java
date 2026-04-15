package com.nutrivista.dto.stats;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 仪表盘全量数据（一次请求返回所有图表所需数据） */
@Getter
@Setter
public class DashboardDto {

    private LocalDate date;
    private int       trendDays;

    // ===== 今日摘要 =====
    private DailyNutritionDto today;

    // ===== 宏量热量占比（今日，%） =====
    private BigDecimal proteinPct;
    private BigDecimal fatPct;
    private BigDecimal carbPct;

    // ===== 统计摘要 =====
    private BigDecimal weekAvgEnergy;
    private int        totalLoggedDays;
    private int        currentStreak;

    // ===== 图表数据 =====
    private List<DailyNutritionDto>    trend;             // 趋势折线图
    private List<NutrientAdequacyDto>  nutrientAdequacy;  // 充足度横向柱状图
    private List<MealDistributionDto>  mealDistribution;  // 餐次分布柱状图
    private List<TopFoodDto>           topFoods;          // 高频食物排行
}
