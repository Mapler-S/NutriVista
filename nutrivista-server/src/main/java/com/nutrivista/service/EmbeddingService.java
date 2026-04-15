package com.nutrivista.service;

import java.util.List;

public interface EmbeddingService {

    /**
     * 获取单条文本的向量表示。
     */
    List<Float> getEmbedding(String text);

    /**
     * 批量获取向量，返回顺序与输入顺序一致。
     * 实现类负责按 API 限制拆分批次。
     */
    List<List<Float>> getBatchEmbeddings(List<String> texts);
}
