package com.nutrivista.dto.meal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateItemRequest {

    @NotNull(message = "重量不能为空")
    @DecimalMin(value = "0.1", message = "重量须大于 0")
    private BigDecimal weightGram;
}
