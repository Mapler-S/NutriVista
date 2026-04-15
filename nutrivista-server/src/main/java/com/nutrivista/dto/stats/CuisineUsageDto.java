package com.nutrivista.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CuisineUsageDto {
    private String cuisine;
    private int    recommendCount;
    private int    adoptedCount;
}
