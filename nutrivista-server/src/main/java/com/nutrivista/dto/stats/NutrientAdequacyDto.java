package com.nutrivista.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 单一营养素充足度（实际摄入 vs 膳食参考摄入量） */
@Getter
@Setter
@AllArgsConstructor
public class NutrientAdequacyDto {
    private String     label;     // 显示名称，如"蛋白质"
    private BigDecimal actual;    // 实际摄入量
    private BigDecimal drv;       // 膳食参考值 (DRV)
    private String     unit;      // 单位
    private BigDecimal pct;       // actual / drv * 100，最大显示 200
}
