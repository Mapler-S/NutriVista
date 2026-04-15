package com.nutrivista.dto.stats;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 高频食物 */
@Getter
@Setter
public class TopFoodDto {
    private Long       foodId;
    private String     nameZh;
    private String     nameEn;
    private int        times;        // 出现次数
    private BigDecimal totalWeight;  // 累计克重
}
