package com.nutrivista.util;

import com.nutrivista.service.MilvusRecipeService.RecipeVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将一道菜谱拆分为 3 个语义 Chunk，每个 Chunk 携带菜名前缀以保证语义完整。
 *
 * <ul>
 *   <li>Chunk 1 — 概述块：name + cuisine + category + tags + description</li>
 *   <li>Chunk 2 — 食材块：name + 所需食材及用量</li>
 *   <li>Chunk 3 — 做法块：name + 步骤 + tips</li>
 * </ul>
 */
public class RecipeVectorizer {

    private static final int MAX_CONTENT_LEN = 2048;

    private RecipeVectorizer() {}

    /**
     * 对单道菜谱生成 3 个 Chunk（不含 embedding，由调用方填充）。
     *
     * @param recipe JSON 解析后的菜谱 Map
     * @return 3 个 {@link RecipeVector}，embedding 字段为 null（待填充）
     */
    @SuppressWarnings("unchecked")
    public static List<RecipeVector> chunkRecipe(Map<String, Object> recipe) {
        String id          = str(recipe, "id");
        String name        = str(recipe, "name");
        String cuisine     = str(recipe, "cuisine");
        String category    = str(recipe, "category");
        String subCategory = str(recipe, "sub_category");
        String difficulty  = str(recipe, "difficulty");
        String description = str(recipe, "description");
        String tips        = str(recipe, "tips");

        List<String>              tags        = listOf(recipe, "tags");
        List<Map<String, Object>> ingredients = (List<Map<String, Object>>) recipe.getOrDefault("ingredients", List.of());
        List<String>              steps       = listOf(recipe, "steps");
        List<String>              suitableFor = listOf(recipe, "suitable_for");

        long calories = ((Number) recipe.getOrDefault("calories_per_serving", 0)).longValue();
        String mealType = String.join(",", suitableFor);

        String chunk1Text = buildOverview(name, cuisine, category, subCategory, tags, description);
        String chunk2Text = buildIngredients(name, ingredients);
        String chunk3Text = buildSteps(name, steps, tips);

        return List.of(
                new RecipeVector(id + "_chunk1", id, null, cuisine, category, mealType, calories, difficulty, truncate(chunk1Text)),
                new RecipeVector(id + "_chunk2", id, null, cuisine, category, mealType, calories, difficulty, truncate(chunk2Text)),
                new RecipeVector(id + "_chunk3", id, null, cuisine, category, mealType, calories, difficulty, truncate(chunk3Text))
        );
    }

    // ── Chunk 构建 ──────────────────────────────────────────────────────────────

    private static String buildOverview(String name, String cuisine, String category,
                                        String subCategory, List<String> tags, String description) {
        StringBuilder sb = new StringBuilder(name);
        append(sb, cuisine);
        append(sb, category);
        if (!subCategory.isBlank()) append(sb, subCategory);
        if (!tags.isEmpty()) append(sb, String.join(" ", tags));
        if (!description.isBlank()) { sb.append(" "); sb.append(description); }
        return sb.toString().trim();
    }

    private static String buildIngredients(String name, List<Map<String, Object>> ingredients) {
        StringBuilder sb = new StringBuilder(name).append(" 所需食材：");
        for (Map<String, Object> ing : ingredients) {
            sb.append(str(ing, "name"))
              .append(str(ing, "amount"))
              .append(str(ing, "unit"))
              .append(" ");
        }
        return sb.toString().trim();
    }

    private static String buildSteps(String name, List<String> steps, String tips) {
        StringBuilder sb = new StringBuilder(name).append(" 做法步骤：");
        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1).append(". ").append(steps.get(i)).append(" ");
        }
        if (!tips.isBlank()) { sb.append("小贴士：").append(tips); }
        return sb.toString().trim();
    }

    // ── 工具方法 ────────────────────────────────────────────────────────────────

    private static void append(StringBuilder sb, String text) {
        if (text != null && !text.isBlank()) { sb.append(" ").append(text); }
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private static List<String> listOf(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) { result.add(item.toString()); }
            return result;
        }
        return List.of();
    }

    private static String truncate(String text) {
        return text.length() > MAX_CONTENT_LEN ? text.substring(0, MAX_CONTENT_LEN) : text;
    }
}
