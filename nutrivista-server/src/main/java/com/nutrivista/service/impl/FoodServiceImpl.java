package com.nutrivista.service.impl;

import com.nutrivista.common.exception.BusinessException;
import com.nutrivista.common.result.PageResult;
import com.nutrivista.dto.food.CategoryTreeDto;
import com.nutrivista.dto.food.FoodDetailDto;
import com.nutrivista.dto.food.FoodListDto;
import com.nutrivista.dto.food.FoodQueryRequest;
import com.nutrivista.dto.food.FoodSuggestionDto;
import com.nutrivista.entity.Food;
import com.nutrivista.mapper.FoodMapper;
import com.nutrivista.repository.FoodCategoryRepository;
import com.nutrivista.repository.FoodRepository;
import com.nutrivista.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;
    private final FoodMapper foodMapper;

    @Override
    public PageResult<FoodListDto> listFoods(FoodQueryRequest request) {
        int page     = Math.max(1, request.getPage()) - 1; // convert to 0-based
        int pageSize = Math.min(100, Math.max(1, request.getPageSize()));

        boolean hasKeyword  = StringUtils.hasText(request.getKeyword());
        boolean hasCategory = request.getCategoryId() != null;
        Page<Food> foodPage;

        if (hasKeyword) {
            // FULLTEXT search: native query, no Spring Sort (MySQL orders by relevance naturally)
            Pageable p = PageRequest.of(page, pageSize);
            String booleanKeyword = buildBooleanKeyword(request.getKeyword());
            foodPage = foodRepository.fullTextSearch(booleanKeyword, p);
        } else if (hasCategory) {
            Pageable p = buildBrowsePageable(page, pageSize);
            foodPage = foodRepository.findByCategoryEager(request.getCategoryId(), p);
        } else {
            Pageable p = buildBrowsePageable(page, pageSize);
            foodPage = foodRepository.findAllActiveEager(p);
        }

        List<FoodListDto> records = foodMapper.toListDtoList(foodPage.getContent());
        return PageResult.of(records, foodPage.getTotalElements(),
                request.getPage(), pageSize);
    }

    @Override
    public FoodDetailDto getFoodDetail(Long id) {
        Food food = foodRepository.findByIdWithNutrition(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "食物不存在: " + id));
        return foodMapper.toDetailDto(food);
    }

    @Override
    public List<CategoryTreeDto> getCategoryTree() {
        return foodMapper.toCategoryTreeDtoList(categoryRepository.findAllRootCategories());
    }

    @Override
    public List<FoodSuggestionDto> getSuggestions(String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 1) {
            return List.of();
        }
        String booleanKeyword = buildBooleanKeyword(keyword.trim());
        return foodMapper.toSuggestionDtoList(foodRepository.findSuggestions(booleanKeyword));
    }

    // ---- helpers ----

    private Pageable buildBrowsePageable(int page, int pageSize) {
        return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Wraps keyword for MySQL BOOLEAN MODE fulltext search.
     * Appends '*' wildcard for prefix matching and quotes multi-word phrases.
     */
    private String buildBooleanKeyword(String raw) {
        String trimmed = raw.trim();
        // If multi-byte (Chinese), search as-is — ngram will match substrings
        // For single token add prefix wildcard for Latin words
        if (trimmed.contains(" ")) {
            return "\"" + trimmed + "\"";
        }
        return trimmed + "*";
    }
}
