package com.nutrivista.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.entity.Recipe;
import com.nutrivista.repository.RecipeRepository;
import com.nutrivista.service.EmbeddingService;
import com.nutrivista.service.MilvusRecipeService;
import com.nutrivista.service.MilvusRecipeService.RecipeVector;
import com.nutrivista.util.RecipeVectorizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜谱数据初始化入口（CommandLineRunner）。
 * 触发条件：{@code recipe.init.enabled=true}（application.yml 中默认 false，
 * 向量化入库完成后无需再次运行）。
 *
 * <p>流程：
 * <ol>
 *   <li>从 classpath 读取 {@code data/recipes/chinese_recipes.json}</li>
 *   <li>创建 Milvus Collection（如不存在）</li>
 *   <li>增量检查 MySQL，跳过已入库菜谱</li>
 *   <li>对新菜谱分块 → 批量向量化 → 写入 Milvus + MySQL</li>
 *   <li>每处理 50 道菜打印一次进度日志</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "recipe.init.enabled", havingValue = "true")
public class RecipeDataInitRunner implements CommandLineRunner {

    /** 每批向量化的 chunk 数量（= 菜谱数 × 3，再按 batchSize 切分） */
    private static final int RECIPE_BATCH = 17;   // 17 道菜 × 3 chunk = 51 条，≤ DashScope 25 需再分
    private static final int LOG_EVERY    = 50;

    private final EmbeddingService      embeddingService;
    private final MilvusRecipeService   milvusRecipeService;
    private final RecipeRepository      recipeRepository;
    private final ObjectMapper          objectMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("========== Recipe Data Initialization START ==========");

        // 1. 扫描所有 recipes_part*.json 文件
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/recipes/recipes_part*.json");
        List<Map<String, Object>> allRecipes = new ArrayList<>();
        for (Resource res : resources) {
            try (InputStream is = res.getInputStream()) {
                List<Map<String, Object>> part = objectMapper.readValue(is, new TypeReference<>() {});
                log.info("读取 {} => {} 道菜谱", res.getFilename(), part.size());
                allRecipes.addAll(part);
            }
        }
        log.info("共读取 {} 道菜谱（来自 {} 个文件）", allRecipes.size(), resources.length);

        // 2. 确保 Milvus Collection 存在
        milvusRecipeService.createCollectionIfNotExists();

        // 3. 分组：MySQL 已有 vs 全新
        List<String> allIds = allRecipes.stream()
                .map(r -> (String) r.get("id"))
                .collect(Collectors.toList());
        Set<String> mysqlExistingIds = recipeRepository.findAllByIdIn(allIds)
                .stream().map(Recipe::getId).collect(Collectors.toSet());

        List<Map<String, Object>> newRecipes = allRecipes.stream()
                .filter(r -> !mysqlExistingIds.contains(r.get("id")))
                .collect(Collectors.toList());
        List<Map<String, Object>> resyncRecipes = allRecipes.stream()
                .filter(r -> mysqlExistingIds.contains(r.get("id")))
                .collect(Collectors.toList());

        log.info("全新菜谱（MySQL + Milvus）: {} 道", newRecipes.size());
        log.info("需补写向量（仅 Milvus）: {} 道", resyncRecipes.size());

        if (newRecipes.isEmpty() && resyncRecipes.isEmpty()) {
            log.info("========== Recipe Data Initialization SKIPPED (all up-to-date) ==========");
            return;
        }

        // 4a. 处理全新菜谱 → 写 Milvus + MySQL
        int totalNew = 0;
        for (int i = 0; i < newRecipes.size(); i += RECIPE_BATCH) {
            List<Map<String, Object>> batch = newRecipes.subList(i, Math.min(i + RECIPE_BATCH, newRecipes.size()));
            processBatch(batch, true);
            totalNew += batch.size();
            if (totalNew % LOG_EVERY == 0 || totalNew == newRecipes.size()) {
                log.info("新增进度: {}/{}", totalNew, newRecipes.size());
            }
        }

        // 4b. 处理 MySQL 已有但 Milvus 缺失的菜谱 → 仅写 Milvus
        int totalResync = 0;
        for (int i = 0; i < resyncRecipes.size(); i += RECIPE_BATCH) {
            List<Map<String, Object>> batch = resyncRecipes.subList(i, Math.min(i + RECIPE_BATCH, resyncRecipes.size()));
            processBatch(batch, false);
            totalResync += batch.size();
            if (totalResync % LOG_EVERY == 0 || totalResync == resyncRecipes.size()) {
                log.info("补写向量进度: {}/{}", totalResync, resyncRecipes.size());
            }
        }

        log.info("========== Recipe Data Initialization COMPLETED. Total: {}, New: {}, Resync: {} ==========",
                allRecipes.size(), totalNew, totalResync);
    }

    /**
     * 处理一批菜谱：分块 → 向量化 → 写 Milvus，按需写 MySQL。
     *
     * @param recipes    原始菜谱数据
     * @param saveToMySql true=同时写 MySQL，false=仅写 Milvus（MySQL 已有数据时）
     */
    @Transactional
    protected void processBatch(List<Map<String, Object>> recipes, boolean saveToMySql) {
        // 所有菜谱展开为 chunks（不含 embedding）
        List<RecipeVector> allChunks = new ArrayList<>();
        for (Map<String, Object> recipe : recipes) {
            allChunks.addAll(RecipeVectorizer.chunkRecipe(recipe));
        }

        // 批量获取 embedding（内部已按 25 条分批）
        List<String> texts = allChunks.stream()
                .map(RecipeVector::textContent)
                .collect(Collectors.toList());
        List<List<Float>> embeddings = embeddingService.getBatchEmbeddings(texts);

        // 填充 embedding
        List<RecipeVector> filledChunks = new ArrayList<>(allChunks.size());
        for (int i = 0; i < allChunks.size(); i++) {
            RecipeVector v = allChunks.get(i);
            filledChunks.add(new RecipeVector(
                    v.chunkId(), v.recipeId(), embeddings.get(i),
                    v.cuisine(), v.category(), v.mealType(),
                    v.calories(), v.difficulty(), v.textContent()));
        }

        // 写入 Milvus（始终执行）
        milvusRecipeService.insertRecipeVectors(filledChunks);

        // 写入 MySQL（仅全新菜谱需要）
        if (saveToMySql) {
            List<Recipe> entities = recipes.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            recipeRepository.saveAll(entities);
        }
    }

    private Recipe toEntity(Map<String, Object> r) {
        try {
            return Recipe.builder()
                    .id(str(r, "id"))
                    .name(str(r, "name"))
                    .nameEn(str(r, "name_en"))
                    .cuisine(str(r, "cuisine"))
                    .category(str(r, "category"))
                    .subCategory(str(r, "sub_category"))
                    .difficulty(str(r, "difficulty"))
                    .prepTimeMinutes(intVal(r, "prep_time_minutes"))
                    .cookTimeMinutes(intVal(r, "cook_time_minutes"))
                    .servings(intVal(r, "servings"))
                    .caloriesPerServing(intVal(r, "calories_per_serving"))
                    .tags(toJson(r.get("tags")))
                    .ingredients(toJson(r.get("ingredients")))
                    .steps(toJson(r.get("steps")))
                    .tips(str(r, "tips"))
                    .nutritionSummary(toJson(r.get("nutrition_summary")))
                    .suitableFor(toJson(r.get("suitable_for")))
                    .description(str(r, "description"))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("转换菜谱 Entity 失败: " + r.get("id"), e);
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    private Integer intVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Number n ? n.intValue() : null;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
