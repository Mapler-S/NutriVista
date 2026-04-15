package com.nutrivista.service;

import com.nutrivista.dto.recipe.RecipeRecommendRequest;
import com.nutrivista.dto.recipe.RecipeRecommendResponse;

public interface RecipeRecommendService {

    /**
     * RAG 菜谱推荐主流程：
     * 查询改写 → Milvus 语义检索 → MySQL 获取完整信息 → LLM 生成推荐结果。
     */
    RecipeRecommendResponse recommend(RecipeRecommendRequest request);
}
