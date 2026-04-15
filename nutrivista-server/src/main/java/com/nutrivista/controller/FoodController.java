package com.nutrivista.controller;

import com.nutrivista.common.result.PageResult;
import com.nutrivista.common.result.Result;
import com.nutrivista.dto.food.CategoryTreeDto;
import com.nutrivista.dto.food.FoodDetailDto;
import com.nutrivista.dto.food.FoodListDto;
import com.nutrivista.dto.food.FoodQueryRequest;
import com.nutrivista.dto.food.FoodSuggestionDto;
import com.nutrivista.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "食物模块", description = "食物查询、分类、搜索接口")
@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @Operation(summary = "分页查询食物列表", description = "支持关键字全文检索、分类筛选、排序")
    @GetMapping
    public Result<PageResult<FoodListDto>> listFoods(FoodQueryRequest request) {
        return Result.success(foodService.listFoods(request));
    }

    @Operation(summary = "获取食物详情", description = "含完整40+项营养成分数据（每100g）")
    @GetMapping("/{id}")
    public Result<FoodDetailDto> getFoodDetail(@PathVariable Long id) {
        return Result.success(foodService.getFoodDetail(id));
    }

    @Operation(summary = "获取食物分类树", description = "返回两级分类结构（大类→子类）")
    @GetMapping("/categories")
    public Result<List<CategoryTreeDto>> getCategoryTree() {
        return Result.success(foodService.getCategoryTree());
    }

    @Operation(summary = "搜索建议", description = "实时下拉补全，返回最多8条建议")
    @GetMapping("/search/suggestions")
    public Result<List<FoodSuggestionDto>> getSuggestions(@RequestParam String keyword) {
        return Result.success(foodService.getSuggestions(keyword));
    }
}
