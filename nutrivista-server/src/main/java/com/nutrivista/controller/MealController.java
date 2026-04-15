package com.nutrivista.controller;

import com.nutrivista.common.result.Result;
import com.nutrivista.dto.meal.AddItemRequest;
import com.nutrivista.dto.meal.DailySummaryDto;
import com.nutrivista.dto.meal.MealItemDto;
import com.nutrivista.dto.meal.UpdateItemRequest;
import com.nutrivista.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "饮食记录模块", description = "餐次记录、食物明细增删改、每日汇总")
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    // 演示阶段固定单用户
    private static final Long DEMO_USER_ID = 1L;

    private final MealService mealService;

    @Operation(summary = "获取某天饮食汇总", description = "返回当天所有餐次及其明细、营养合计")
    @GetMapping("/daily-summary")
    public Result<DailySummaryDto> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(mealService.getDailySummary(DEMO_USER_ID, date));
    }

    @Operation(summary = "添加食物到餐次", description = "不存在该餐次则自动创建 MealRecord")
    @PostMapping("/items")
    public Result<MealItemDto> addItem(@RequestBody @Valid AddItemRequest request) {
        return Result.success(mealService.addItem(DEMO_USER_ID, request));
    }

    @Operation(summary = "更新食物克重")
    @PatchMapping("/items/{id}")
    public Result<MealItemDto> updateItem(
            @PathVariable Long id,
            @RequestBody @Valid UpdateItemRequest request) {
        return Result.success(mealService.updateItem(id, request));
    }

    @Operation(summary = "删除食物明细")
    @DeleteMapping("/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id) {
        mealService.removeItem(id);
        return Result.success();
    }

    @Operation(summary = "删除整个餐次记录")
    @DeleteMapping("/{id}")
    public Result<Void> removeMeal(@PathVariable Long id) {
        mealService.removeMeal(id);
        return Result.success();
    }
}
