package com.nutrivista.controller;

import com.nutrivista.common.result.Result;
import com.nutrivista.dto.stats.*;
import com.nutrivista.entity.RecipeUsageLog;
import com.nutrivista.repository.RecipeUsageLogRepository;
import com.nutrivista.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Tag(name = "统计分析模块", description = "仪表盘数据：趋势、宏量分布、营养充足度、高频食物")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private static final Long DEMO_USER_ID = 1L;

    private final StatsService             statsService;
    private final RecipeUsageLogRepository recipeUsageLogRepository;

    @Operation(summary = "仪表盘全量数据", description = "一次返回趋势、宏量、充足度、餐次分布、高频食物")
    @GetMapping("/dashboard")
    public Result<DashboardDto> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") int days) {
        LocalDate target = date != null ? date : LocalDate.now();
        int clampedDays  = Math.min(90, Math.max(7, days));
        return Result.success(statsService.getDashboard(DEMO_USER_ID, target, clampedDays));
    }

    @Operation(summary = "菜谱使用统计", description = "推荐次数、菜系分布、采用率趋势、热门菜谱 Top-10")
    @GetMapping("/recipe-usage")
    public Result<RecipeUsageStatsVO> getRecipeUsageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        long totalRec     = recipeUsageLogRepository.countByUserIdAndEventTypeAndMealDateBetween(
                DEMO_USER_ID, RecipeUsageLog.EventType.RECOMMEND, startDate, endDate);
        long totalAdopted = recipeUsageLogRepository.countByUserIdAndEventTypeAndMealDateBetween(
                DEMO_USER_ID, RecipeUsageLog.EventType.ADOPTED, startDate, endDate);
        double rate = totalRec == 0 ? 0.0
                : Math.round((double) totalAdopted / totalRec * 1000.0) / 10.0;

        // ── 菜系聚合 ────────────────────────────────────────────────────────────
        List<Object[]> cuisineRows = recipeUsageLogRepository
                .countByCuisineAndEventType(DEMO_USER_ID, startDate, endDate);

        Map<String, int[]> cuisineMap = new LinkedHashMap<>();   // cuisine → [recommendCnt, adoptedCnt]
        for (Object[] row : cuisineRows) {
            String cuisine   = (String) row[0];
            String eventType = (String) row[1];
            int    cnt       = ((Number) row[2]).intValue();
            int[]  arr       = cuisineMap.computeIfAbsent(cuisine, k -> new int[2]);
            if ("RECOMMEND".equals(eventType)) arr[0] += cnt;
            else                               arr[1] += cnt;
        }
        List<CuisineUsageDto> cuisineStats = new ArrayList<>();
        cuisineMap.forEach((c, arr) -> cuisineStats.add(new CuisineUsageDto(c, arr[0], arr[1])));
        cuisineStats.sort(Comparator.comparingInt((CuisineUsageDto d) -> d.getRecommendCount()).reversed());

        // ── Top 采用菜谱 ─────────────────────────────────────────────────────────
        List<TopRecipeUsageDto> topRecipes = recipeUsageLogRepository
                .findTopAdoptedRecipes(DEMO_USER_ID, startDate, endDate, 10)
                .stream()
                .map(r -> new TopRecipeUsageDto(
                        (String) r[0],
                        (String) r[1],
                        (String) r[2],
                        ((Number) r[3]).intValue()))
                .toList();

        // ── 按日期趋势 ────────────────────────────────────────────────────────────
        List<Object[]> trendRows = recipeUsageLogRepository
                .countByDateAndEventType(DEMO_USER_ID, startDate, endDate);

        Map<LocalDate, int[]> trendMap = new TreeMap<>();  // date → [recommendCnt, adoptedCnt]
        for (Object[] row : trendRows) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            String eventType = (String) row[1];
            int    cnt       = ((Number) row[2]).intValue();
            int[]  arr       = trendMap.computeIfAbsent(date, k -> new int[2]);
            if ("RECOMMEND".equals(eventType)) arr[0] += cnt;
            else                               arr[1] += cnt;
        }
        List<DailyUsageTrendDto> dailyTrend = new ArrayList<>();
        trendMap.forEach((date, arr) -> {
            double dayRate = arr[0] == 0 ? 0.0
                    : Math.round((double) arr[1] / arr[0] * 1000.0) / 10.0;
            dailyTrend.add(new DailyUsageTrendDto(date, arr[0], arr[1], dayRate));
        });

        return Result.success(RecipeUsageStatsVO.builder()
                .totalRecommendations((int) totalRec)
                .totalAdopted((int) totalAdopted)
                .adoptionRatePct(rate)
                .cuisineStats(cuisineStats)
                .topRecipes(topRecipes)
                .dailyTrend(dailyTrend)
                .build());
    }
}
