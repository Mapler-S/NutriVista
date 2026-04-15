package com.nutrivista.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * AI 服务接口 — 预留扩展接口
 *
 * <p>未来接入大语言模型（如 Claude API、GPT-4 等）提供智能饮食分析功能。
 * 当前阶段接口结构已定义，具体实现待 Phase 5+ 完成。</p>
 */
public interface AiService {

    /**
     * AI 饮食综合分析
     *
     * @param userId    用户 ID
     * @param startDate 分析开始日期
     * @param endDate   分析结束日期
     * @return AI 分析报告（结构化 JSON）
     */
    Map<String, Object> analyzeDiet(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * AI 个性化饮食建议
     *
     * @param userId 用户 ID
     * @return 饮食建议列表
     */
    Map<String, Object> recommend(Long userId);

    /**
     * AI 营养趋势预测
     *
     * @param userId   用户 ID
     * @param nutrient 营养素名称
     * @param days     预测天数
     * @return 预测数据
     */
    Map<String, Object> predictTrend(Long userId, String nutrient, int days);
}
