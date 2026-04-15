package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 搜索下拉建议（极简） */
@Getter
@Setter
public class FoodSuggestionDto {
    private Long id;
    private String nameZh;
    private String nameEn;
    private String categoryNameZh;
    private BigDecimal energyKcal;
}
