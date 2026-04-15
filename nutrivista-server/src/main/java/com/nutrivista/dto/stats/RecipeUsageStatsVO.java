package com.nutrivista.dto.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeUsageStatsVO {
    /** 时段内被推荐展示的菜谱总次数 */
    private int totalRecommendations;
    /** 时段内实际添加到饮食记录的次数 */
    private int totalAdopted;
    /** 整体采用率百分比（0–100） */
    private double adoptionRatePct;
    /** 各菜系推荐 vs 采用次数（雷达图数据） */
    private List<CuisineUsageDto>     cuisineStats;
    /** 最常被采用的菜谱 Top-10 */
    private List<TopRecipeUsageDto>   topRecipes;
    /** 按日期的推荐/采用趋势（折线图数据） */
    private List<DailyUsageTrendDto>  dailyTrend;
}
