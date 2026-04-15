package com.nutrivista.service;

import com.nutrivista.dto.meal.AddItemRequest;
import com.nutrivista.dto.meal.DailySummaryDto;
import com.nutrivista.dto.meal.MealItemDto;
import com.nutrivista.dto.meal.UpdateItemRequest;

import java.time.LocalDate;

public interface MealService {

    /** 获取某天的饮食汇总（含所有餐次及其明细） */
    DailySummaryDto getDailySummary(Long userId, LocalDate date);

    /** 向指定餐次添加食物（不存在则自动创建 MealRecord） */
    MealItemDto addItem(Long userId, AddItemRequest request);

    /** 更新食物克重 */
    MealItemDto updateItem(Long itemId, UpdateItemRequest request);

    /** 删除单条食物明细 */
    void removeItem(Long itemId);

    /** 删除整个餐次记录（含所有明细） */
    void removeMeal(Long mealId);
}
