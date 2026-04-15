package com.nutrivista.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrivista.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 调用 DeepSeek Chat API（兼容 OpenAI 格式）。
 * base-url 可替换为任意 OpenAI-compatible 端点（如通义千问、OpenAI）。
 */
@Slf4j
@Service
public class DeepSeekLlmServiceImpl implements LlmService {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model:deepseek-chat}")
    private String model;

    @Value("${llm.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${llm.max-tokens:2048}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private double temperature;

    private static final int MAX_RETRIES = 2;
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return callWithRetry(systemPrompt, userMessage);
    }

    @Override
    public String chatWithContext(String systemPrompt, String context, String userMessage) {
        String combined = "【参考信息】\n" + context + "\n\n【用户问题】\n" + userMessage;
        return callWithRetry(systemPrompt, combined);
    }

    private String callWithRetry(String systemPrompt, String userMessage) {
        Exception last = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return doCall(systemPrompt, userMessage);
            } catch (Exception e) {
                last = e;
                log.warn("LLM 调用失败 (第 {}/{} 次): {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) sleep(1500L * attempt);
            }
        }
        throw new RuntimeException("LLM 调用失败，已重试 " + MAX_RETRIES + " 次", last);
    }

    private String doCall(String systemPrompt, String userMessage) throws IOException {
        String url = baseUrl.stripTrailing() + "/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", maxTokens,
                "temperature", temperature
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(body), JSON_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("LLM API 错误 " + response.code() + ": " + responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 记录 token 消耗
            JsonNode usage = root.path("usage");
            if (!usage.isMissingNode()) {
                log.info("LLM token 消耗 — prompt: {}, completion: {}, total: {}",
                        usage.path("prompt_tokens").asInt(),
                        usage.path("completion_tokens").asInt(),
                        usage.path("total_tokens").asInt());
            }
            return content;
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
