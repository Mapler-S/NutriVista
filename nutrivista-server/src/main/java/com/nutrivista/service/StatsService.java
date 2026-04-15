package com.nutrivista.service;

import com.nutrivista.dto.stats.DashboardDto;

import java.time.LocalDate;

public interface StatsService {

    /**
     * 一次返回仪表盘所需全量数据：
     * 今日摘要、趋势、宏量占比、营养充足度、餐次分布、高频食物
     *
     * @param userId  用户ID（演示阶段传 1）
     * @param date    基准日期（通常为今天）
     * @param days    趋势天数（7 / 14 / 30）
     */
    DashboardDto getDashboard(Long userId, LocalDate date, int days);
}
