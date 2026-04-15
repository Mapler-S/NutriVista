package com.nutrivista.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyUsageTrendDto {
    private LocalDate date;
    private int       recommended;
    private int       adopted;
    /** 采用率百分比，recommended=0 时为 0 */
    private double    adoptionRatePct;
}
