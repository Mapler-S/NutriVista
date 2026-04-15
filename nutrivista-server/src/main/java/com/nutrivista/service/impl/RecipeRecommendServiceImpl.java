package com.nutrivista.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.dto.recipe.*;
import com.nutrivista.entity.Recipe;
import com.nutrivista.repository.RecipeRepository;
import com.nutrivista.service.EmbeddingService;
import com.nutrivista.service.LlmService;
import com.nutrivista.service.MilvusRecipeService;
import com.nutrivista.service.MilvusRecipeService.RecipeSearchResult;
import com.nutrivista.service.RecipeRecommendService;
import com.nutrivista.util.RecipePromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeRecommendServiceImpl implements RecipeRecommendService {

    /** Milvus 初次检索条数（含过滤后再由 LLM 精选） */
    private static final int VECTOR_SEARCH_TOP_K = 20;
    /** 传给 LLM 的最多候选菜谱数 */
    private static final int MAX_LLM_CANDIDATES  = 10;

    private final MilvusRecipeService milvusRecipeService;
    private final EmbeddingService    embeddingService;
    private final LlmService          llmService;
    private final RecipeRepository    recipeRepository;
    private final ObjectMapper        objectMapper;

    @Override
    @Transactional(readOnly = true)
    public RecipeRecommendResponse recommend(RecipeRecommendRequest request) {
        // ── 1. 查询改写 ──────────────────────────────────────────────────────────
        String expandedQuery = RecipePromptBuilder.expandQuery(request);
        log.debug("RAG query: [{}] → expanded: [{}]", request.getUserQuery(), expandedQuery);

        // ── 2. Milvus 语义检索（Top-20，含标量过滤） ─────────────────────────────
        String mealTypeZh = RecipePromptBuilder.mealTypeZh(request.getMealType());
        List<RecipeSearchResult> searchResults = milvusRecipeService.searchWithFilters(
                expandedQuery,
                VECTOR_SEARCH_TOP_K,
                request.getPreferredCuisine(),
                mealTypeZh.isBlank() ? null : mealTypeZh,
                request.getMaxCalories()
        );
        log.debug("Milvus 返回 {} 条结果", searchResults.size());

        if (searchResults.isEmpty()) {
            return RecipeRecommendResponse.builder()
                    .recipes(List.of())
                    .aiSummary("暂未找到符合条件的菜谱，请尝试放宽筛选条件。")
                    .advice(new NutritionAdvice("建议多样化饮食，保证营养均衡。"))
                    .build();
        }

        // ── 3. 按 recipe_id 去重，保留最高分 ────────────────────────────────────
        Map<String, Double> scoreByRecipeId = new LinkedHashMap<>();
        for (RecipeSearchResult r : searchResults) {
            scoreByRecipeId.merge(r.recipeId(), (double) r.score(), Math::max);
        }

        // 按分数降序取前 MAX_LLM_CANDIDATES 个 recipe_id
        List<String> topRecipeIds = scoreByRecipeId.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(MAX_LLM_CANDIDATES)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // ── 4. MySQL 获取完整菜谱信息 ────────────────────────────────────────────
        Map<String, Recipe> recipeMap = recipeRepository.findAllById(topRecipeIds)
                .stream().collect(Collectors.toMap(Recipe::getId, r -> r));

        // 按 topRecipeIds 顺序排列（保持相关度排序）
        List<Recipe> orderedCandidates = topRecipeIds.stream()
                .filter(recipeMap::containsKey)
                .map(recipeMap::get)
                .collect(Collectors.toList());

        if (orderedCandidates.isEmpty()) {
            return fallbackResponse(request, scoreByRecipeId, List.of());
        }

        // ── 5. 调用 LLM ──────────────────────────────────────────────────────────
        String systemPrompt = RecipePromptBuilder.buildSystemPrompt();
        String userPrompt   = RecipePromptBuilder.buildUserPrompt(request, orderedCandidates, scoreByRecipeId);

        String llmRaw;
        try {
            llmRaw = llmService.chat(systemPrompt, userPrompt);
            log.debug("LLM 原始输出: {}", llmRaw);
        } catch (Exception e) {
            log.warn("LLM 调用失败，降级为直接返回 Milvus Top 结果: {}", e.getMessage());
            return fallbackResponse(request, scoreByRecipeId, orderedCandidates);
        }

        // ── 6. 解析 LLM JSON 输出 ────────────────────────────────────────────────
        try {
            return parseLlmResponse(llmRaw, recipeMap, scoreByRecipeId, request.getCount());
        } catch (Exception e) {
            log.warn("LLM 输出解析失败，降级: {} | 原始内容: {}", e.getMessage(), llmRaw);
            return fallbackResponse(request, scoreByRecipeId, orderedCandidates);
        }
    }

    // ── LLM 响应解析 ─────────────────────────────────────────────────────────────

    private RecipeRecommendResponse parseLlmResponse(String llmRaw,
                                                      Map<String, Recipe> recipeMap,
                                                      Map<String, Double> scoreMap,
                                                      int wantCount) throws Exception {
        // 提取 JSON（防止 LLM 多输出了 ```json ... ``` 包装）
        String json = extractJson(llmRaw);
        JsonNode root = objectMapper.readTree(json);

        List<RecommendedRecipe> result = new ArrayList<>();
        for (JsonNode item : root.path("recipes")) {
            String recipeId = item.path("recipe_id").asText();
            Recipe recipe = recipeMap.get(recipeId);
            if (recipe == null) continue;

            result.add(RecommendedRecipe.builder()
                    .recipeId(recipeId)
                    .name(recipe.getName())
                    .cuisine(recipe.getCuisine())
                    .category(recipe.getCategory())
                    .calories(recipe.getCaloriesPerServing())
                    .cookTime(recipe.getCookTimeMinutes())
                    .servings(recipe.getServings())
                    .difficulty(recipe.getDifficulty())
                    .matchReason(item.path("match_reason").asText())
                    .nutritionComment(item.path("nutrition_comment").asText())
                    .relevanceScore(scoreMap.getOrDefault(recipeId, 0.0))
                    .ingredients(parseIngredients(recipe.getIngredients()))
                    .steps(parseSteps(recipe.getSteps()))
                    .nutrition(parseNutrition(recipe.getNutritionSummary()))
                    .build());

            if (result.size() >= wantCount) break;
        }

        return RecipeRecommendResponse.builder()
                .recipes(result)
                .aiSummary(root.path("summary").asText(""))
                .advice(new NutritionAdvice(root.path("nutrition_advice").asText("")))
                .build();
    }

    /** 去除 LLM 可能附加的 ```json ... ``` 包装，提取纯 JSON */
    private String extractJson(String raw) {
        String s = raw.strip();
        if (s.startsWith("```")) {
            int start = s.indexOf('\n') + 1;
            int end   = s.lastIndexOf("```");
            if (end > start) s = s.substring(start, end).strip();
        }
        return s;
    }

    // ── 降级响应（LLM 不可用时直接返回 Milvus Top 结果） ────────────────────────

    private RecipeRecommendResponse fallbackResponse(RecipeRecommendRequest request,
                                                      Map<String, Double> scoreMap,
                                                      List<Recipe> candidates) {
        int want = request.getCount() != null ? request.getCount() : 3;
        List<RecommendedRecipe> recipes = candidates.stream()
                .limit(want)
                .map(r -> RecommendedRecipe.builder()
                        .recipeId(r.getId())
                        .name(r.getName())
                        .cuisine(r.getCuisine())
                        .category(r.getCategory())
                        .calories(r.getCaloriesPerServing())
                        .cookTime(r.getCookTimeMinutes())
                        .servings(r.getServings())
                        .difficulty(r.getDifficulty())
                        .matchReason("根据语义相关度推荐")
                        .relevanceScore(scoreMap.getOrDefault(r.getId(), 0.0))
                        .ingredients(parseIngredients(r.getIngredients()))
                        .steps(parseSteps(r.getSteps()))
                        .nutrition(parseNutrition(r.getNutritionSummary()))
                        .build())
                .collect(Collectors.toList());

        return RecipeRecommendResponse.builder()
                .recipes(recipes)
                .aiSummary("以下是根据您的需求匹配到的菜谱。")
                .advice(new NutritionAdvice("建议多样化饮食，保证营养均衡。"))
                .build();
    }

    // ── JSON 字段解析 ────────────────────────────────────────────────────────────

    private List<RecipeIngredient> parseIngredients(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<>() {});
            return list.stream().map(m -> {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setName(str(m, "name"));
                ri.setAmount(str(m, "amount"));
                ri.setUnit(str(m, "unit"));
                Object foodId = m.get("food_id");
                if (foodId instanceof Number n) ri.setFoodId(n.longValue());
                return ri;
            }).collect(Collectors.toList());
        } catch (Exception e) { return List.of(); }
    }

    private List<String> parseSteps(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }

    private NutritionSummary parseNutrition(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            JsonNode n = objectMapper.readTree(json);
            NutritionSummary ns = new NutritionSummary();
            ns.setProteinG(nodeDouble(n, "protein_g"));
            ns.setFatG(nodeDouble(n, "fat_g"));
            ns.setCarbG(nodeDouble(n, "carb_g"));
            ns.setFiberG(nodeDouble(n, "fiber_g"));
            return ns;
        } catch (Exception e) { return null; }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private Double nodeDouble(JsonNode n, String field) {
        JsonNode v = n.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asDouble();
    }
}
