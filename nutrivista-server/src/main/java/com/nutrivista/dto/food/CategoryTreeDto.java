package com.nutrivista.dto.food;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryTreeDto {
    private Integer id;
    private String nameZh;
    private String nameEn;
    private String icon;
    private Integer sortOrder;
    private List<CategoryTreeDto> children;
}
