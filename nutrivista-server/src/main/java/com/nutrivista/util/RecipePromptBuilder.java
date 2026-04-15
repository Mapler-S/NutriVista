package com.nutrivista.util;

import com.nutrivista.dto.recipe.DailyNutritionSummary;
import com.nutrivista.dto.recipe.RecipeRecommendRequest;
import com.nutrivista.entity.Recipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 构造 RAG 推荐所用的 System Prompt 和 User Prompt。
 */
public class RecipePromptBuilder {

    private RecipePromptBuilder() {}

    public static String buildSystemPrompt() {
        return """
                你是 NutriVista 系统的专业营养师和中国菜谱推荐助手。
                你的任务是根据用户的需求和提供的候选菜谱信息，推荐最合适的菜谱。

                推荐原则：
                1. 优先匹配用户的口味偏好和用餐场景
                2. 考虑营养均衡，结合用户当天已有的营养摄入情况
                3. 考虑烹饪难度和时间可行性
                4. 如果用户有排除食材（如过敏），严格排除含有该食材的菜谱
                5. 给出每道推荐菜的具体理由

                输出要求：
                请严格按照以下 JSON 格式输出，不要添加任何代码块标记或其他内容：
                {
                  "recipes": [
                    {
                      "recipe_id": "xxx",
                      "match_reason": "推荐理由（1-2句话）",
                      "nutrition_comment": "营养点评（1句话）"
                    }
                  ],
                  "summary": "整体推荐总结（1-2句话）",
                  "nutrition_advice": "营养搭配建议（1-2句话）"
                }
                """;
    }

    /**
     * 构建 User Prompt，包含：用户需求 + 候选菜谱上下文 + 约束条件。
     *
     * @param request    用户请求
     * @param candidates 候选菜谱（MySQL 完整信息）
     * @param scoreMap   recipe_id → Milvus 相似度分数
     */
    public static String buildUserPrompt(RecipeRecommendRequest request,
                                         List<Recipe> candidates,
                                         Map<String, Double> scoreMap) {
        StringBuilder sb = new StringBuilder();

        // 1. 用户需求
        sb.append("【用户需求】\n");
        if (hasText(request.getUserQuery())) {
            sb.append("描述：").append(request.getUserQuery()).append("\n");
        }
        if (hasText(request.getMealType())) {
            sb.append("餐次：").append(mealTypeZh(request.getMealType())).append("\n");
        }
        if (hasText(request.getPreferredCuisine())) {
            sb.append("偏好菜系：").append(request.getPreferredCuisine()).append("\n");
        }
        if (request.getMaxCalories() != null) {
            sb.append("热量上限：").append(request.getMaxCalories()).append(" kcal\n");
        }
        if (hasText(request.getDifficulty())) {
            sb.append("难度偏好：").append(request.getDifficulty()).append("\n");
        }
        if (request.getExcludeIngredients() != null && !request.getExcludeIngredients().isEmpty()) {
            sb.append("排除食材：").append(String.join("、", request.getExcludeIngredients())).append("\n");
        }
        if (request.getCount() != null) {
            sb.append("期望推荐数量：").append(request.getCount()).append(" 道\n");
        }

        // 2. 当日营养摄入（营养互补上下文）
        DailyNutritionSummary intake = request.getExistingIntake();
        if (intake != null) {
            sb.append("\n【当日已摄入营养】\n");
            appendIfNotNull(sb, "热量", intake.getEnergyKcal(), " kcal");
            appendIfNotNull(sb, "蛋白质", intake.getProteinG(), " g");
            appendIfNotNull(sb, "脂肪", intake.getFatG(), " g");
            appendIfNotNull(sb, "碳水化合物", intake.getCarbG(), " g");
            appendIfNotNull(sb, "膳食纤维", intake.getFiberG(), " g");
        }

        // 3. 候选菜谱
        sb.append("\n【候选菜谱（按语义相关度排序）】\n");
        for (Recipe r : candidates) {
            double score = scoreMap.getOrDefault(r.getId(), 0.0);
            sb.append("---\n");
            sb.append("菜谱ID: ").append(r.getId()).append("\n");
            sb.append("名称: ").append(r.getName()).append("\n");
            sb.append("菜系: ").append(orEmpty(r.getCuisine())).append("  分类: ").append(orEmpty(r.getCategory())).append("\n");
            sb.append("热量: ").append(r.getCaloriesPerServing()).append(" kcal/份  烹饪时间: ")
              .append(r.getCookTimeMinutes()).append(" 分钟  难度: ").append(orEmpty(r.getDifficulty())).append("\n");
            sb.append("语义相似度: ").append(String.format("%.3f", score)).append("\n");
            if (hasText(r.getDescription())) {
                sb.append("简介: ").append(r.getDescription(), 0, Math.min(r.getDescription().length(), 100)).append("\n");
            }
        }

        sb.append("\n请从以上候选菜谱中选出最合适的 ").append(request.getCount()).append(" 道推荐，按 JSON 格式输出。");
        return sb.toString();
    }

    /** 将英文 mealType 转换为中文展示 */
    public static String mealTypeZh(String mealType) {
        if (mealType == null) return "";
        return switch (mealType.toUpperCase()) {
            case "BREAKFAST"      -> "早餐";
            case "MORNING_SNACK"  -> "上午加餐";
            case "LUNCH"          -> "午餐";
            case "AFTERNOON_TEA"  -> "下午茶";
            case "DINNER"         -> "晚餐";
            case "SUPPER"         -> "夜宵";
            case "SNACK"          -> "加餐";
            default               -> mealType;
        };
    }

    /**
     * 查询改写：将简短用户输入扩展为更丰富的语义搜索文本。
     */
    public static String expandQuery(RecipeRecommendRequest request) {
        StringBuilder q = new StringBuilder();
        if (hasText(request.getUserQuery())) q.append(request.getUserQuery()).append(" ");
        if (hasText(request.getMealType()))  q.append(mealTypeZh(request.getMealType())).append(" ");
        if (hasText(request.getPreferredCuisine())) q.append(request.getPreferredCuisine()).append(" ");
        if (hasText(request.getDifficulty())) {
            q.append(switch (request.getDifficulty().toLowerCase()) {
                case "easy"   -> "简单快手 ";
                case "hard"   -> "精致复杂 ";
                default       -> "";
            });
        }
        if (request.getMaxCalories() != null && request.getMaxCalories() <= 400) {
            q.append("低热量 清淡 ");
        }
        if (request.getExcludeIngredients() != null && !request.getExcludeIngredients().isEmpty()) {
            String excludes = request.getExcludeIngredients().stream()
                    .map(s -> "不含" + s).collect(Collectors.joining(" "));
            q.append(excludes).append(" ");
        }
        String result = q.toString().trim();
        return result.isBlank() ? "家常菜" : result;
    }

    // ── 工具方法 ────────────────────────────────────────────────────────────────

    private static boolean hasText(String s) { return s != null && !s.isBlank(); }
    private static String orEmpty(String s)  { return s != null ? s : ""; }

    private static void appendIfNotNull(StringBuilder sb, String label, Object value, String unit) {
        if (value != null) sb.append(label).append(": ").append(value).append(unit).append("\n");
    }
}
