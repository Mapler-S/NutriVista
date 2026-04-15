package com.nutrivista.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusRecipeService {

    private final MilvusServiceClient milvusClient;
    private final EmbeddingService embeddingService;

    @Value("${milvus.collection-name}")
    private String collectionName;

    @Value("${milvus.dimension}")
    private int dimension;

    // ── 数据模型 ────────────────────────────────────────────────────────────────

    public record RecipeVector(
            String chunkId,
            String recipeId,
            List<Float> embedding,
            String cuisine,
            String category,
            String mealType,
            long calories,
            String difficulty,
            String textContent
    ) {}

    public record RecipeSearchResult(
            String recipeId,
            String chunkId,
            String textContent,
            String cuisine,
            String category,
            float score
    ) {}

    // ── Collection 管理 ─────────────────────────────────────────────────────────

    /**
     * 若 Collection 不存在则创建，并建立向量索引后加载到内存。
     */
    public void createCollectionIfNotExists() {
        R<Boolean> hasResp = milvusClient.hasCollection(
                HasCollectionParam.newBuilder().withCollectionName(collectionName).build());
        checkStatus(hasResp, "hasCollection");

        if (Boolean.TRUE.equals(hasResp.getData())) {
            log.info("Milvus collection '{}' 已存在，跳过创建", collectionName);
            loadCollection();
            return;
        }

        // 字段定义
        List<FieldType> fields = List.of(
                FieldType.newBuilder()
                        .withName("chunk_id").withDataType(DataType.VarChar).withMaxLength(128)
                        .withPrimaryKey(true).withAutoID(false).build(),
                FieldType.newBuilder()
                        .withName("recipe_id").withDataType(DataType.VarChar).withMaxLength(64).build(),
                FieldType.newBuilder()
                        .withName("embedding").withDataType(DataType.FloatVector).withDimension(dimension).build(),
                FieldType.newBuilder()
                        .withName("cuisine").withDataType(DataType.VarChar).withMaxLength(32).build(),
                FieldType.newBuilder()
                        .withName("category").withDataType(DataType.VarChar).withMaxLength(32).build(),
                FieldType.newBuilder()
                        .withName("meal_type").withDataType(DataType.VarChar).withMaxLength(64).build(),
                FieldType.newBuilder()
                        .withName("calories").withDataType(DataType.Int64).build(),
                FieldType.newBuilder()
                        .withName("difficulty").withDataType(DataType.VarChar).withMaxLength(16).build(),
                FieldType.newBuilder()
                        .withName("text_content").withDataType(DataType.VarChar).withMaxLength(2048).build()
        );

        CreateCollectionParam.Builder createBuilder = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("菜谱语义向量库")
                .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED);
        fields.forEach(createBuilder::addFieldType);
        CreateCollectionParam createParam = createBuilder.build();
        checkStatus(milvusClient.createCollection(createParam), "createCollection");
        log.info("Milvus collection '{}' 创建成功", collectionName);

        // 向量索引：IVF_FLAT + COSINE（数据量 <10 万适用）
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.COSINE)
                .withExtraParam("{\"nlist\":128}")
                .build();
        checkStatus(milvusClient.createIndex(indexParam), "createIndex");
        log.info("向量索引创建成功");

        loadCollection();
    }

    private void loadCollection() {
        checkStatus(
                milvusClient.loadCollection(
                        LoadCollectionParam.newBuilder().withCollectionName(collectionName).build()),
                "loadCollection");
        log.info("Collection '{}' 已加载到内存", collectionName);
    }

    // ── 写入 ────────────────────────────────────────────────────────────────────

    /**
     * 批量插入向量数据。
     */
    public void insertRecipeVectors(List<RecipeVector> vectors) {
        if (vectors.isEmpty()) return;

        List<String> chunkIds = new ArrayList<>();
        List<String> recipeIds = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();
        List<String> cuisines = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> mealTypes = new ArrayList<>();
        List<Long> caloriesList = new ArrayList<>();
        List<String> difficulties = new ArrayList<>();
        List<String> textContents = new ArrayList<>();

        for (RecipeVector v : vectors) {
            chunkIds.add(v.chunkId());
            recipeIds.add(v.recipeId());
            embeddings.add(v.embedding());
            cuisines.add(v.cuisine());
            categories.add(v.category());
            mealTypes.add(v.mealType());
            caloriesList.add(v.calories());
            difficulties.add(v.difficulty());
            textContents.add(v.textContent());
        }

        List<InsertParam.Field> fields = List.of(
                new InsertParam.Field("chunk_id", chunkIds),
                new InsertParam.Field("recipe_id", recipeIds),
                new InsertParam.Field("embedding", embeddings),
                new InsertParam.Field("cuisine", cuisines),
                new InsertParam.Field("category", categories),
                new InsertParam.Field("meal_type", mealTypes),
                new InsertParam.Field("calories", caloriesList),
                new InsertParam.Field("difficulty", difficulties),
                new InsertParam.Field("text_content", textContents)
        );

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<MutationResult> resp = milvusClient.insert(insertParam);
        checkStatus(resp, "insert");
        log.debug("插入 {} 条向量", vectors.size());
    }

    // ── 搜索 ────────────────────────────────────────────────────────────────────

    /**
     * 语义搜索，支持可选的 scalar 过滤表达式。
     *
     * @param queryText 查询文本（自动向量化）
     * @param topK      返回条数
     * @param expr      Milvus 布尔过滤表达式，null 表示不过滤
     *                  示例：{@code "cuisine == \"川菜\" && calories <= 500"}
     */
    public List<RecipeSearchResult> searchSimilarRecipes(String queryText, int topK, String expr) {
        List<Float> queryVector = embeddingService.getEmbedding(queryText);
        return doSearch(List.of(queryVector), topK, expr);
    }

    /**
     * 带结构化过滤的语义搜索（常用参数版）。
     */
    public List<RecipeSearchResult> searchWithFilters(
            String queryText, int topK, String cuisine, String mealType, Integer maxCalories) {

        List<String> conditions = new ArrayList<>();
        if (cuisine != null && !cuisine.isBlank()) {
            conditions.add("cuisine == \"" + cuisine + "\"");
        }
        if (mealType != null && !mealType.isBlank()) {
            conditions.add("meal_type like \"%" + mealType + "%\"");
        }
        if (maxCalories != null) {
            conditions.add("calories <= " + maxCalories);
        }
        String expr = conditions.isEmpty() ? null : String.join(" && ", conditions);
        return searchSimilarRecipes(queryText, topK, expr);
    }

    private List<RecipeSearchResult> doSearch(List<List<Float>> vectors, int topK, String expr) {
        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withMetricType(MetricType.COSINE)
                .withTopK(topK)
                .withFloatVectors(vectors)
                .withVectorFieldName("embedding")
                .withOutFields(List.of("recipe_id", "chunk_id", "text_content", "cuisine", "category"))
                .withParams("{\"nprobe\":16}")
                .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED);

        if (expr != null && !expr.isBlank()) {
            builder.withExpr(expr);
        }

        R<SearchResults> resp = milvusClient.search(builder.build());
        checkStatus(resp, "search");

        SearchResultsWrapper wrapper = new SearchResultsWrapper(resp.getData().getResults());
        List<RecipeSearchResult> results = new ArrayList<>();

        List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
        for (SearchResultsWrapper.IDScore score : scores) {
            String chunkId = (String) score.getStrID();
            String recipeId = (String) wrapper.getFieldData("recipe_id", 0)
                    .get(scores.indexOf(score));
            String textContent = (String) wrapper.getFieldData("text_content", 0)
                    .get(scores.indexOf(score));
            String c = (String) wrapper.getFieldData("cuisine", 0)
                    .get(scores.indexOf(score));
            String cat = (String) wrapper.getFieldData("category", 0)
                    .get(scores.indexOf(score));
            results.add(new RecipeSearchResult(recipeId, chunkId, textContent, c, cat, score.getScore()));
        }
        return results;
    }

    // ── 删除 / 维护 ─────────────────────────────────────────────────────────────

    /**
     * 删除某道菜谱的所有向量块。
     */
    public void deleteByRecipeId(String recipeId) {
        String expr = "recipe_id == \"" + recipeId + "\"";
        DeleteParam deleteParam = DeleteParam.newBuilder()
                .withCollectionName(collectionName)
                .withExpr(expr)
                .build();
        checkStatus(milvusClient.delete(deleteParam), "delete");
        log.info("已删除 recipe_id={} 的所有向量", recipeId);
    }

    /**
     * 重建向量索引（先 drop 再 create）。
     */
    public void rebuildIndex() {
        log.info("开始重建 Milvus 索引...");
        milvusClient.dropIndex(io.milvus.param.index.DropIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withIndexName("embedding")
                .build());

        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.COSINE)
                .withExtraParam("{\"nlist\":128}")
                .build();
        checkStatus(milvusClient.createIndex(indexParam), "rebuildIndex");
        loadCollection();
        log.info("Milvus 索引重建完成");
    }

    // ── 工具方法 ────────────────────────────────────────────────────────────────

    private <T> void checkStatus(R<T> response, String operation) {
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(
                    "Milvus 操作失败 [" + operation + "]: " + response.getMessage());
        }
    }
}
