package com.nutrivista.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.dto.stats.DailyNutritionDto;
import com.nutrivista.dto.stats.DashboardDto;
import com.nutrivista.dto.stats.NutrientAdequacyDto;
import com.nutrivista.service.AiService;
import com.nutrivista.service.LlmService;
import com.nutrivista.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * AI 饮食分析实现 —— 基于 StatsService 聚合数据 + LlmService 结构化输出。
 *
 * <p>策略：</p>
 * <ul>
 *   <li>从 StatsService 拿到结构化营养数据（趋势、充足度、宏量占比等）；</li>
 *   <li>用紧凑 JSON 喂给 LLM，限定返回 JSON 格式；</li>
 *   <li>LLM 不可用时给出规则式降级响应，保证接口始终可用。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final StatsService  statsService;
    private final LlmService    llmService;
    private final ObjectMapper  objectMapper;

    // ============================================================
    // 1. 饮食综合分析
    // ============================================================
    @Override
    public Map<String, Object> analyzeDiet(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("日期范围非法");
        }
        int days = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        DashboardDto dashboard = statsService.getDashboard(userId, endDate, days);

        String systemPrompt = """
                你是一位资深注册营养师。请基于用户提供的饮食统计数据，给出客观、具体、可执行的饮食评估。
                严格返回 JSON，不要包含 Markdown 代码块标记。字段：
                {
                  "summary": "整体饮食评价（100字内）",
                  "score": 0-100 的整数综合评分,
                  "strengths": ["饮食亮点1", "饮食亮点2"],
                  "issues":    ["潜在问题1", "潜在问题2"],
                  "advice":    ["改进建议1", "改进建议2", "改进建议3"]
                }
                """;
        String userPrompt = """
                【分析时间段】%s 至 %s（共 %d 天）
                【记录天数】%d 天，连续打卡 %d 天
                【宏量热量占比】蛋白质 %s%%，脂肪 %s%%，碳水 %s%%
                【本周平均热量】%s kcal/天
                【今日营养摄入】%s
                【关键营养素充足度】%s
                """.formatted(
                        startDate, endDate, days,
                        dashboard.getTotalLoggedDays(), dashboard.getCurrentStreak(),
                        dashboard.getProteinPct(), dashboard.getFatPct(), dashboard.getCarbPct(),
                        dashboard.getWeekAvgEnergy(),
                        formatNutrition(dashboard.getToday()),
                        formatAdequacy(dashboard.getNutrientAdequacy()));

        Map<String, Object> llmResult = callLlmJson(systemPrompt, userPrompt);
        if (llmResult == null) {
            llmResult = ruleBasedAnalysis(dashboard);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("days", days);
        result.put("analysis", llmResult);
        return result;
    }

    // ============================================================
    // 2. 个性化饮食建议
    // ============================================================
    @Override
    public Map<String, Object> recommend(Long userId) {
        LocalDate today = LocalDate.now();
        DashboardDto dashboard = statsService.getDashboard(userId, today, 7);

        String systemPrompt = """
                你是一位营养师，基于用户最近 7 天的饮食数据，给出个性化饮食建议。
                严格返回 JSON，不要包含 Markdown 代码块标记。字段：
                {
                  "focus": "本周首要改进方向（一句话）",
                  "recommendations": [
                    {"title": "建议标题", "reason": "原因", "action": "具体行动"}
                  ],
                  "foods_to_add":    ["建议增加的食物1", "食物2"],
                  "foods_to_reduce": ["建议减少的食物1", "食物2"]
                }
                """;
        String userPrompt = """
                【最近7天平均热量】%s kcal/天
                【当前宏量占比】蛋白质 %s%%，脂肪 %s%%，碳水 %s%%
                【营养素充足度】%s
                【高频食物 Top】%s
                """.formatted(
                        dashboard.getWeekAvgEnergy(),
                        dashboard.getProteinPct(), dashboard.getFatPct(), dashboard.getCarbPct(),
                        formatAdequacy(dashboard.getNutrientAdequacy()),
                        formatTopFoods(dashboard));

        Map<String, Object> llmResult = callLlmJson(systemPrompt, userPrompt);
        if (llmResult == null) {
            llmResult = ruleBasedRecommend(dashboard);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("date", today.toString());
        result.put("recommendation", llmResult);
        return result;
    }

    // ============================================================
    // 3. 营养趋势预测
    // ============================================================
    @Override
    public Map<String, Object> predictTrend(Long userId, String nutrient, int days) {
        if (nutrient == null || nutrient.isBlank()) {
            throw new IllegalArgumentException("营养素名称不能为空");
        }
        if (days <= 0 || days > 30) {
            throw new IllegalArgumentException("预测天数必须在 1-30 之间");
        }

        LocalDate today = LocalDate.now();
        int lookbackDays = Math.max(14, days * 2);
        DashboardDto dashboard = statsService.getDashboard(userId, today, lookbackDays);

        List<BigDecimal> history = new ArrayList<>();
        for (DailyNutritionDto d : dashboard.getTrend()) {
            history.add(nutrientValue(d, nutrient));
        }

        // 基于简单移动平均 + 最近趋势斜率的规则式预测（兜底主力）
        List<Map<String, Object>> forecast = simpleForecast(today, history, days);

        // 让 LLM 补充解读（可选，不影响数值）
        String interpretation = llmInterpret(nutrient, history, forecast);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("nutrient", nutrient);
        result.put("days", days);
        result.put("historyDays", history.size());
        result.put("forecast", forecast);
        result.put("interpretation", interpretation);
        return result;
    }

    // ============================================================
    // helpers — LLM 调用与 JSON 解析
    // ============================================================

    /** 调用 LLM，要求返回 JSON；失败或解析失败返回 null 交由上层降级。 */
    private Map<String, Object> callLlmJson(String systemPrompt, String userPrompt) {
        try {
            String raw = llmService.chat(systemPrompt, userPrompt);
            String json = stripCodeFence(raw);
            JsonNode node = objectMapper.readTree(json);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(node, Map.class);
            return map;
        } catch (Exception e) {
            log.warn("AI 调用或解析失败，启用规则降级: {}", e.getMessage());
            return null;
        }
    }

    private String stripCodeFence(String raw) {
        String s = raw == null ? "" : raw.strip();
        if (s.startsWith("```")) {
            int start = s.indexOf('\n') + 1;
            int end = s.lastIndexOf("```");
            if (end > start) s = s.substring(start, end).strip();
        }
        return s;
    }

    private String llmInterpret(String nutrient, List<BigDecimal> history, List<Map<String, Object>> forecast) {
        try {
            String prompt = "营养素 [%s] 近 %d 天摄入：%s；预测未来 %d 天：%s。请用一句话（不超过60字）给出趋势解读与建议。"
                    .formatted(nutrient, history.size(), history, forecast.size(),
                            forecast.stream().map(m -> m.get("value")).toList());
            return llmService.chat("你是一位简明的营养师。只输出结论，不要解释。", prompt).strip();
        } catch (Exception e) {
            return "根据历史记录估算的趋势值，仅供参考。";
        }
    }

    // ============================================================
    // helpers — 数据格式化
    // ============================================================

    private String formatNutrition(DailyNutritionDto d) {
        if (d == null) return "无记录";
        return "热量 %s kcal, 蛋白 %s g, 脂肪 %s g, 碳水 %s g, 纤维 %s g"
                .formatted(d.getEnergyKcal(), d.getProtein(), d.getFat(),
                        d.getCarbohydrate(), d.getFiber());
    }

    private String formatAdequacy(List<NutrientAdequacyDto> list) {
        if (list == null || list.isEmpty()) return "无数据";
        StringBuilder sb = new StringBuilder();
        for (NutrientAdequacyDto a : list) {
            sb.append(a.getLabel()).append(' ')
              .append(a.getPct()).append("%; ");
        }
        return sb.toString();
    }

    private String formatTopFoods(DashboardDto dashboard) {
        if (dashboard.getTopFoods() == null || dashboard.getTopFoods().isEmpty()) return "无";
        StringBuilder sb = new StringBuilder();
        dashboard.getTopFoods().stream().limit(5).forEach(f ->
                sb.append(f.getNameZh()).append("(").append(f.getTimes()).append("次), "));
        return sb.toString();
    }

    private BigDecimal nutrientValue(DailyNutritionDto d, String nutrient) {
        return switch (nutrient) {
            case "energyKcal", "energy", "热量"   -> d.getEnergyKcal();
            case "protein", "蛋白质"              -> d.getProtein();
            case "fat", "脂肪"                    -> d.getFat();
            case "carbohydrate", "carb", "碳水"   -> d.getCarbohydrate();
            case "fiber", "膳食纤维"              -> d.getFiber();
            case "vitaminC"                       -> d.getVitaminC();
            case "calcium", "钙"                  -> d.getCalcium();
            case "iron", "铁"                     -> d.getIron();
            case "zinc", "锌"                     -> d.getZinc();
            case "vitaminA"                       -> d.getVitaminA();
            case "vitaminB1"                      -> d.getVitaminB1();
            case "vitaminB2"                      -> d.getVitaminB2();
            default -> throw new IllegalArgumentException("不支持的营养素: " + nutrient);
        };
    }

    // ============================================================
    // helpers — 规则式预测与兜底
    // ============================================================

    /** 线性外推：近 7 天均值 + 简单斜率，结果非负。 */
    private List<Map<String, Object>> simpleForecast(LocalDate today, List<BigDecimal> history, int days) {
        List<Map<String, Object>> forecast = new ArrayList<>();
        int n = history.size();
        if (n == 0) {
            for (int i = 1; i <= days; i++) {
                forecast.add(Map.of("date", today.plusDays(i).toString(), "value", BigDecimal.ZERO));
            }
            return forecast;
        }

        int window = Math.min(7, n);
        BigDecimal recentAvg = history.subList(n - window, n).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(window), 2, RoundingMode.HALF_UP);

        BigDecimal slope = BigDecimal.ZERO;
        if (n >= 4) {
            int half = window / 2;
            BigDecimal earlyAvg = history.subList(n - window, n - half).stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(window - half), 4, RoundingMode.HALF_UP);
            BigDecimal lateAvg = history.subList(n - half, n).stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(half), 4, RoundingMode.HALF_UP);
            slope = lateAvg.subtract(earlyAvg)
                    .divide(BigDecimal.valueOf(Math.max(half, 1)), 4, RoundingMode.HALF_UP);
        }

        for (int i = 1; i <= days; i++) {
            BigDecimal v = recentAvg.add(slope.multiply(BigDecimal.valueOf(i)))
                    .setScale(2, RoundingMode.HALF_UP);
            if (v.compareTo(BigDecimal.ZERO) < 0) v = BigDecimal.ZERO;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", today.plusDays(i).toString());
            point.put("value", v);
            forecast.add(point);
        }
        return forecast;
    }

    private Map<String, Object> ruleBasedAnalysis(DashboardDto d) {
        List<String> strengths = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        List<String> advice = new ArrayList<>();

        if (d.getCurrentStreak() >= 3) strengths.add("连续打卡 " + d.getCurrentStreak() + " 天，坚持良好");
        if (d.getProteinPct() != null && d.getProteinPct().doubleValue() >= 15) strengths.add("蛋白质供能比例合理");

        if (d.getProteinPct() != null && d.getProteinPct().doubleValue() < 12) {
            issues.add("蛋白质摄入偏低");
            advice.add("增加鸡蛋、鱼、豆制品等优质蛋白来源");
        }
        if (d.getFatPct() != null && d.getFatPct().doubleValue() > 35) {
            issues.add("脂肪供能占比偏高");
            advice.add("减少油炸、高脂加工食品");
        }
        if (d.getNutrientAdequacy() != null) {
            for (NutrientAdequacyDto a : d.getNutrientAdequacy()) {
                if (a.getPct() != null && a.getPct().doubleValue() < 60) {
                    issues.add(a.getLabel() + "摄入不足（" + a.getPct() + "%）");
                }
            }
        }
        if (advice.isEmpty()) advice.add("保持饮食多样化，每日摄入蔬果 300g 以上");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("summary", "已基于规则生成评估（AI 服务暂不可用）");
        r.put("score", Math.max(50, 100 - issues.size() * 10));
        r.put("strengths", strengths);
        r.put("issues", issues);
        r.put("advice", advice);
        return r;
    }

    private Map<String, Object> ruleBasedRecommend(DashboardDto d) {
        List<String> add = new ArrayList<>();
        List<String> reduce = new ArrayList<>();
        List<Map<String, String>> recs = new ArrayList<>();

        if (d.getNutrientAdequacy() != null) {
            for (NutrientAdequacyDto a : d.getNutrientAdequacy()) {
                if (a.getPct() != null && a.getPct().doubleValue() < 70) {
                    recs.add(Map.of(
                            "title", "补充" + a.getLabel(),
                            "reason", a.getLabel() + "摄入仅 " + a.getPct() + "%",
                            "action", "安排富含" + a.getLabel() + "的食物进入日常膳食"));
                }
            }
        }
        if (d.getFatPct() != null && d.getFatPct().doubleValue() > 35) reduce.add("油炸食品");
        if (add.isEmpty()) add.add("深色蔬菜");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("focus", recs.isEmpty() ? "保持当前均衡饮食" : "优先补齐关键微量营养素");
        r.put("recommendations", recs);
        r.put("foods_to_add", add);
        r.put("foods_to_reduce", reduce);
        return r;
    }
}
