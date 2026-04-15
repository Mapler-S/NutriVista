package com.nutrivista.dto.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 批量导入结果摘要 */
@Getter
@Setter
public class ImportResultDto {
    private int          total;
    private int          success;
    private int          skipped;
    private List<String> errors;
}
