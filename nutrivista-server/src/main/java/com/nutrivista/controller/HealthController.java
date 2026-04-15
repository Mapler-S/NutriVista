package com.nutrivista.controller;

import com.nutrivista.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 健康检查接口 — 用于验证前后端联通性
 */
@Tag(name = "Health Check", description = "系统健康检查")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "健康检查", description = "返回服务运行状态，用于前后端联通性验证")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "service", "NutriVista API Server",
                "version", "1.0.0",
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "message", "NutriVista 服务运行正常 🎉"
        ));
    }

    @Operation(summary = "Ping", description = "简单连通性测试")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
