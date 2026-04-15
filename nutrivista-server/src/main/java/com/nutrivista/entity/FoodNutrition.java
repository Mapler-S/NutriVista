package com.nutrivista.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 食物营养成分表（与 food 共享主键，1:1 映射）
 * 所有营养值基于每 100g 可食部，null 表示数据缺失
 */
@Getter
@Setter
@Entity
@Table(name = "food_nutrition")
public class FoodNutrition {

    /** 与 food.id 共享主键（@MapsId 策略） */
    @Id
    @Column(name = "food_id")
    private Long foodId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "food_id")
    private Food food;

    // ===== 基础宏量营养素 =====
    @Column(name = "energy_kcal", precision = 8, scale = 2) private BigDecimal energyKcal;
    @Column(name = "energy_kj",   precision = 8, scale = 2) private BigDecimal energyKj;
    @Column(name = "water",       precision = 8, scale = 2) private BigDecimal water;
    @Column(name = "protein",     precision = 8, scale = 2) private BigDecimal protein;
    @Column(name = "fat",         precision = 8, scale = 2) private BigDecimal fat;
    @Column(name = "carbohydrate",precision = 8, scale = 2) private BigDecimal carbohydrate;
    @Column(name = "fiber",       precision = 8, scale = 2) private BigDecimal fiber;
    @Column(name = "sugar",       precision = 8, scale = 2) private BigDecimal sugar;
    @Column(name = "starch",      precision = 8, scale = 2) private BigDecimal starch;
    @Column(name = "ash",         precision = 8, scale = 2) private BigDecimal ash;

    // ===== 脂肪酸 =====
    @Column(name = "saturated_fat",       precision = 8, scale = 2) private BigDecimal saturatedFat;
    @Column(name = "monounsaturated_fat", precision = 8, scale = 2) private BigDecimal monounsaturatedFat;
    @Column(name = "polyunsaturated_fat", precision = 8, scale = 2) private BigDecimal polyunsaturatedFat;
    @Column(name = "trans_fat",           precision = 8, scale = 2) private BigDecimal transFat;
    @Column(name = "cholesterol",         precision = 8, scale = 2) private BigDecimal cholesterol;

    // ===== 矿物质 =====
    @Column(name = "calcium",    precision = 8, scale = 2) private BigDecimal calcium;
    @Column(name = "iron",       precision = 8, scale = 3) private BigDecimal iron;
    @Column(name = "magnesium",  precision = 8, scale = 2) private BigDecimal magnesium;
    @Column(name = "phosphorus", precision = 8, scale = 2) private BigDecimal phosphorus;
    @Column(name = "potassium",  precision = 8, scale = 2) private BigDecimal potassium;
    @Column(name = "sodium",     precision = 8, scale = 2) private BigDecimal sodium;
    @Column(name = "zinc",       precision = 8, scale = 3) private BigDecimal zinc;
    @Column(name = "copper",     precision = 8, scale = 4) private BigDecimal copper;
    @Column(name = "manganese",  precision = 8, scale = 4) private BigDecimal manganese;
    @Column(name = "selenium",   precision = 8, scale = 3) private BigDecimal selenium;
    @Column(name = "iodine",     precision = 8, scale = 3) private BigDecimal iodine;
    @Column(name = "chromium",   precision = 8, scale = 3) private BigDecimal chromium;

    // ===== 脂溶性维生素 =====
    @Column(name = "vitamin_a",      precision = 10, scale = 2) private BigDecimal vitaminA;
    @Column(name = "beta_carotene",  precision = 10, scale = 2) private BigDecimal betaCarotene;
    @Column(name = "vitamin_d",      precision = 8,  scale = 3) private BigDecimal vitaminD;
    @Column(name = "vitamin_e",      precision = 8,  scale = 2) private BigDecimal vitaminE;
    @Column(name = "vitamin_k",      precision = 8,  scale = 2) private BigDecimal vitaminK;

    // ===== 水溶性维生素 =====
    @Column(name = "vitamin_c",  precision = 8, scale = 2) private BigDecimal vitaminC;
    @Column(name = "vitamin_b1", precision = 8, scale = 3) private BigDecimal vitaminB1;
    @Column(name = "vitamin_b2", precision = 8, scale = 3) private BigDecimal vitaminB2;
    @Column(name = "vitamin_b3", precision = 8, scale = 2) private BigDecimal vitaminB3;
    @Column(name = "vitamin_b5", precision = 8, scale = 3) private BigDecimal vitaminB5;
    @Column(name = "vitamin_b6", precision = 8, scale = 3) private BigDecimal vitaminB6;
    @Column(name = "vitamin_b12",precision = 10,scale = 3) private BigDecimal vitaminB12;
    @Column(name = "folate",     precision = 8, scale = 2) private BigDecimal folate;
    @Column(name = "biotin",     precision = 8, scale = 3) private BigDecimal biotin;
}
