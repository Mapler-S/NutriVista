package com.nutrivista.mapper;

import com.nutrivista.dto.food.CategoryTreeDto;
import com.nutrivista.dto.food.FoodDetailDto;
import com.nutrivista.dto.food.FoodListDto;
import com.nutrivista.dto.food.FoodSuggestionDto;
import com.nutrivista.dto.food.NutritionDto;
import com.nutrivista.entity.Food;
import com.nutrivista.entity.FoodCategory;
import com.nutrivista.entity.FoodNutrition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // ---- FoodListDto ----
    @Mapping(source = "category.id",     target = "categoryId")
    @Mapping(source = "category.nameZh", target = "categoryNameZh")
    @Mapping(source = "nutrition.energyKcal",   target = "energyKcal")
    @Mapping(source = "nutrition.protein",      target = "protein")
    @Mapping(source = "nutrition.fat",          target = "fat")
    @Mapping(source = "nutrition.carbohydrate", target = "carbohydrate")
    @Mapping(source = "nutrition.fiber",        target = "fiber")
    FoodListDto toListDto(Food food);

    List<FoodListDto> toListDtoList(List<Food> foods);

    // ---- FoodDetailDto ----
    @Mapping(source = "category.id",     target = "categoryId")
    @Mapping(source = "category.nameZh", target = "categoryNameZh")
    @Mapping(source = "category.nameEn", target = "categoryNameEn")
    @Mapping(source = "nutrition",       target = "nutrition")
    FoodDetailDto toDetailDto(Food food);

    // ---- NutritionDto ----
    NutritionDto toNutritionDto(FoodNutrition nutrition);

    // ---- FoodSuggestionDto ----
    @Mapping(source = "category.nameZh",        target = "categoryNameZh")
    @Mapping(source = "nutrition.energyKcal",   target = "energyKcal")
    FoodSuggestionDto toSuggestionDto(Food food);

    List<FoodSuggestionDto> toSuggestionDtoList(List<Food> foods);

    // ---- CategoryTreeDto ----
    @Mapping(source = "children", target = "children")
    CategoryTreeDto toCategoryTreeDto(FoodCategory category);

    List<CategoryTreeDto> toCategoryTreeDtoList(List<FoodCategory> categories);
}
