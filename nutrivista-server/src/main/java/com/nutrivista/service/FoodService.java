package com.nutrivista.service;

import com.nutrivista.common.result.PageResult;
import com.nutrivista.dto.food.CategoryTreeDto;
import com.nutrivista.dto.food.FoodDetailDto;
import com.nutrivista.dto.food.FoodListDto;
import com.nutrivista.dto.food.FoodQueryRequest;
import com.nutrivista.dto.food.FoodSuggestionDto;

import java.util.List;

public interface FoodService {

    /** 分页查询食物列表（支持关键字全文检索、分类筛选） */
    PageResult<FoodListDto> listFoods(FoodQueryRequest request);

    /** 查询食物详情（含完整营养成分） */
    FoodDetailDto getFoodDetail(Long id);

    /** 获取全量分类树（大类 → 子类，两级） */
    List<CategoryTreeDto> getCategoryTree();

    /** 搜索建议（前8条，用于下拉补全） */
    List<FoodSuggestionDto> getSuggestions(String keyword);
}
