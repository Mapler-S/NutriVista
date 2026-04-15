package com.nutrivista.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 餐次类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum MealType {

    BREAKFAST("早餐", 1),
    MORNING_SNACK("上午加餐", 2),
    LUNCH("午餐", 3),
    AFTERNOON_TEA("下午茶", 4),
    DINNER("晚餐", 5),
    SUPPER("夜宵", 6);

    private final String displayName;
    private final int sortOrder;
}
