package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

/** 食物列表查询参数 */
@Getter
@Setter
public class FoodQueryRequest {

    private String keyword;
    private Integer categoryId;
    private Integer page = 1;
    private Integer pageSize = 20;
    /** 可选排序字段：energy_kcal / protein / fat / carbohydrate（默认按id升序） */
    private String sortBy;
}
