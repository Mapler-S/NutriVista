package com.nutrivista.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 调用阿里云 DashScope text-embedding-v3 获取中文向量。
 * 文档: https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-api-details
 *
 * 备选方案: 若使用本地模型（如 BGE-large-zh-v1.5 + ONNX Runtime），
 * 只需实现 EmbeddingService 接口并替换此 Bean 即可。
 */
@Slf4j
@Service
public class DashScopeEmbeddingServiceImpl implements EmbeddingService {

    private static final String API_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";
    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.get("application/json; charset=utf-8");

    @Value("${embedding.api-key}")
    private String apiKey;

    @Value("${embedding.model:text-embedding-v3}")
    private String model;

    @Value("${embedding.dimension:1024}")
    private int dimension;

    @Value("${embedding.batch-size:10}")
    private int batchSize;

    @Value("${embedding.max-retries:3}")
    private int maxRetries;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Float> getEmbedding(String text) {
        List<List<Float>> results = getBatchEmbeddings(List.of(text));
        return results.get(0);
    }

    @Override
    public List<List<Float>> getBatchEmbeddings(List<String> texts) {
        List<List<Float>> allResults = new ArrayList<>(texts.size());

        // 按批次拆分（DashScope 单批最多 batchSize 条）
        for (int i = 0; i < texts.size(); i += batchSize) {
            List<String> batch = texts.subList(i, Math.min(i + batchSize, texts.size()));
            List<List<Float>> batchResult = callApiWithRetry(batch);
            allResults.addAll(batchResult);
        }
        return allResults;
    }

    private List<List<Float>> callApiWithRetry(List<String> batch) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return callApi(batch);
            } catch (Exception e) {
                lastException = e;
                log.warn("DashScope embedding 调用失败 (第 {}/{} 次): {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    sleepExponential(attempt);
                }
            }
        }
        throw new RuntimeException("DashScope embedding 调用失败，已重试 " + maxRetries + " 次", lastException);
    }

    private List<List<Float>> callApi(List<String> texts) throws IOException {
        // 构建请求 JSON
        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("model", model);
            put("input", new java.util.LinkedHashMap<>() {{
                put("texts", texts);
            }});
            put("parameters", new java.util.LinkedHashMap<>() {{
                put("dimension", dimension);
                put("output_type", "dense");
            }});
        }});

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "(empty)";
                throw new IOException("DashScope API 返回错误 " + response.code() + ": " + errorBody);
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode embeddings = root.path("output").path("embeddings");

            // 按 text_index 排序后返回
            List<List<Float>> result = new ArrayList<>(texts.size());
            for (int i = 0; i < texts.size(); i++) {
                result.add(null);
            }
            for (JsonNode item : embeddings) {
                int idx = item.get("text_index").asInt();
                List<Float> vector = new ArrayList<>();
                for (JsonNode val : item.get("embedding")) {
                    vector.add(val.floatValue());
                }
                result.set(idx, vector);
            }
            return result;
        }
    }

    private void sleepExponential(int attempt) {
        try {
            long millis = 1000L * (1L << (attempt - 1)); // 1s, 2s, 4s
            Thread.sleep(Math.min(millis, 8000));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
