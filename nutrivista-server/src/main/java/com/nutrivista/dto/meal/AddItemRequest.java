package com.nutrivista.dto.meal;

import com.nutrivista.common.constant.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AddItemRequest {

    @NotNull(message = "日期不能为空")
    private LocalDate mealDate;

    @NotNull(message = "餐次类型不能为空")
    private MealType mealType;

    @NotNull(message = "食物ID不能为空")
    private Long foodId;

    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.1", message = "重量须大于 0")
    private BigDecimal weightGram;
}
