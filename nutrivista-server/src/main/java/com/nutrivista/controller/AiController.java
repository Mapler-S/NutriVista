package com.nutrivista.controller;

import com.nutrivista.common.result.Result;
import com.nutrivista.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * AI 分析接口 —— 基于 LLM 的饮食分析、推荐与趋势预测。
 */
@Tag(name = "AI 分析", description = "AI 驱动的饮食分析、个性化建议与趋势预测")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    private static final long DEFAULT_USER_ID = 1L;

    @Operation(summary = "AI 饮食综合分析")
    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyze(@RequestBody Map<String, Object> request) {
        Long userId = longValue(request.get("userId"), DEFAULT_USER_ID);
        LocalDate endDate   = dateValue(request.get("endDate"),   LocalDate.now());
        LocalDate startDate = dateValue(request.get("startDate"), endDate.minusDays(6));
        return Result.success(aiService.analyzeDiet(userId, startDate, endDate));
    }

    @Operation(summary = "AI 个性化饮食建议")
    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody Map<String, Object> request) {
        Long userId = longValue(request.get("userId"), DEFAULT_USER_ID);
        return Result.success(aiService.recommend(userId));
    }

    @Operation(summary = "AI 营养趋势预测")
    @PostMapping("/predict")
    public Result<Map<String, Object>> predict(@RequestBody Map<String, Object> request) {
        Long userId = longValue(request.get("userId"), DEFAULT_USER_ID);
        String nutrient = String.valueOf(request.getOrDefault("nutrient", "energyKcal"));
        int days = intValue(request.get("days"), 7);
        return Result.success(aiService.predictTrend(userId, nutrient, days));
    }

    // ---------- 简易参数解析 ----------

    private Long longValue(Object v, Long def) {
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) return Long.parseLong(s.trim());
        return def;
    }

    private int intValue(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s && !s.isBlank()) return Integer.parseInt(s.trim());
        return def;
    }

    private LocalDate dateValue(Object v, LocalDate def) {
        if (v instanceof String s && !s.isBlank()) return LocalDate.parse(s.trim());
        return def;
    }
}
