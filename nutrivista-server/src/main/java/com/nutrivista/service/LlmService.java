package com.nutrivista.service;

public interface LlmService {

    /**
     * 单轮对话。
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 携带检索上下文的对话（RAG 推荐专用）。
     * context 会被插入 user message 中，位于用户问题之前。
     */
    String chatWithContext(String systemPrompt, String context, String userMessage);
}
