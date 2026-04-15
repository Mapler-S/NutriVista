package com.nutrivista.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.common.result.Result;
import com.nutrivista.dto.data.ExportRowDto;
import com.nutrivista.dto.data.ImportResultDto;
import com.nutrivista.service.DataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Tag(name = "数据管理模块", description = "饮食数据导入/导出")
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private static final Long DEMO_USER_ID = 1L;

    private final DataService  dataService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "导出饮食数据", description = "format=csv（默认）或 json，最长跨度 2 年")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "csv") String format) throws Exception {

        if (to.isBefore(from)) { LocalDate tmp = from; from = to; to = tmp; }
        if (ChronoUnit.DAYS.between(from, to) > 730) to = from.plusDays(730);

        byte[]    body;
        MediaType mediaType;
        String    filename;

        if ("json".equalsIgnoreCase(format)) {
            List<ExportRowDto> data = dataService.exportData(DEMO_USER_ID, from, to);
            body      = objectMapper.writeValueAsBytes(data);
            mediaType = MediaType.APPLICATION_JSON;
            filename  = "nutrivista_" + from + "_" + to + ".json";
        } else {
            String csv = dataService.exportAsCsv(DEMO_USER_ID, from, to);
            body      = csv.getBytes(StandardCharsets.UTF_8);
            mediaType = new MediaType("text", "csv", StandardCharsets.UTF_8);
            filename  = "nutrivista_" + from + "_" + to + ".csv";
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @Operation(summary = "导入饮食数据", description = "上传 CSV，列格式与导出模板一致；营养列可留空")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ImportResultDto> importData(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.badRequest("文件不能为空");
        return Result.success(dataService.importFromCsv(DEMO_USER_ID, file));
    }

    @Operation(summary = "下载导入模板", description = "含表头和示例行的空白 CSV")
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = "\uFEFF日期,餐次,食物名称,重量(g),能量(kcal),蛋白质(g),脂肪(g),碳水化合物(g),膳食纤维(g)\n"
                   + "2026-03-31,早餐,鸡蛋,60,,,,,\n"
                   + "2026-03-31,午餐,米饭,150,,,,,\n"
                   + "2026-03-31,晚餐,苹果,200,,,,,\n";
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"nutrivista_import_template.csv\"")
                .body(body);
    }
}
