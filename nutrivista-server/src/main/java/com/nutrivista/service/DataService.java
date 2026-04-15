package com.nutrivista.service;

import com.nutrivista.dto.data.ExportRowDto;
import com.nutrivista.dto.data.ImportResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface DataService {

    /** 查询用户指定日期范围内的所有饮食数据，返回结构化行 */
    List<ExportRowDto> exportData(Long userId, LocalDate from, LocalDate to);

    /** 将 exportData 结果序列化为 UTF-8 BOM CSV 字符串 */
    String exportAsCsv(Long userId, LocalDate from, LocalDate to);

    /** 解析 CSV 文件，批量导入饮食记录，返回结果摘要 */
    ImportResultDto importFromCsv(Long userId, MultipartFile file);
}
