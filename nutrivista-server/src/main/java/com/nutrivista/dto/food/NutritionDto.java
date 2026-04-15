package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NutritionDto {

    // 基础宏量
    private BigDecimal energyKcal;
    private BigDecimal energyKj;
    private BigDecimal water;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal fiber;
    private BigDecimal sugar;
    private BigDecimal starch;
    private BigDecimal ash;

    // 脂肪酸
    private BigDecimal saturatedFat;
    private BigDecimal monounsaturatedFat;
    private BigDecimal polyunsaturatedFat;
    private BigDecimal transFat;
    private BigDecimal cholesterol;

    // 矿物质
    private BigDecimal calcium;
    private BigDecimal iron;
    private BigDecimal magnesium;
    private BigDecimal phosphorus;
    private BigDecimal potassium;
    private BigDecimal sodium;
    private BigDecimal zinc;
    private BigDecimal copper;
    private BigDecimal manganese;
    private BigDecimal selenium;
    private BigDecimal iodine;
    private BigDecimal chromium;

    // 脂溶性维生素
    private BigDecimal vitaminA;
    private BigDecimal betaCarotene;
    private BigDecimal vitaminD;
    private BigDecimal vitaminE;
    private BigDecimal vitaminK;

    // 水溶性维生素
    private BigDecimal vitaminC;
    private BigDecimal vitaminB1;
    private BigDecimal vitaminB2;
    private BigDecimal vitaminB3;
    private BigDecimal vitaminB5;
    private BigDecimal vitaminB6;
    private BigDecimal vitaminB12;
    private BigDecimal folate;
    private BigDecimal biotin;
}
