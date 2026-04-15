package com.nutrivista.repository;

import com.nutrivista.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * FULLTEXT boolean mode search — ngram parser supports Chinese substring.
     * Returns active foods only.
     */
    @Query(value = """
            SELECT * FROM food
            WHERE is_active = 1
              AND MATCH(name_zh, name_en, alias) AGAINST (:keyword IN BOOLEAN MODE)
            """,
           countQuery = """
            SELECT COUNT(*) FROM food
            WHERE is_active = 1
              AND MATCH(name_zh, name_en, alias) AGAINST (:keyword IN BOOLEAN MODE)
            """,
           nativeQuery = true)
    Page<Food> fullTextSearch(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Autocomplete suggestions — top 8 results by relevance score.
     */
    @Query(value = """
            SELECT * FROM food
            WHERE is_active = 1
              AND MATCH(name_zh, name_en, alias) AGAINST (:keyword IN BOOLEAN MODE)
            ORDER BY MATCH(name_zh, name_en, alias) AGAINST (:keyword IN BOOLEAN MODE) DESC
            LIMIT 8
            """,
           nativeQuery = true)
    List<Food> findSuggestions(@Param("keyword") String keyword);

    /** Browse by sub-category — eager-fetch category + nutrition to avoid N+1 */
    @Query(value = """
            SELECT f FROM Food f
            JOIN FETCH f.category
            JOIN FETCH f.nutrition
            WHERE f.category.id = :categoryId AND f.isActive = true
            """,
           countQuery = "SELECT COUNT(f) FROM Food f WHERE f.category.id = :categoryId AND f.isActive = true")
    Page<Food> findByCategoryEager(@Param("categoryId") Integer categoryId, Pageable pageable);

    /** Browse all active foods — eager-fetch category + nutrition to avoid N+1 */
    @Query(value = """
            SELECT f FROM Food f
            JOIN FETCH f.category
            JOIN FETCH f.nutrition
            WHERE f.isActive = true
            """,
           countQuery = "SELECT COUNT(f) FROM Food f WHERE f.isActive = true")
    Page<Food> findAllActiveEager(Pageable pageable);

    /** Fetch food with nutrition in one query */
    @Query("SELECT f FROM Food f JOIN FETCH f.nutrition WHERE f.id = :id AND f.isActive = true")
    Optional<Food> findByIdWithNutrition(@Param("id") Long id);

    /** 按中文名精确匹配，用于 CSV 导入时查找食物 */
    Optional<Food> findByNameZhAndIsActiveTrue(String nameZh);

    /**
     * 反向子串匹配：食物名称（name_zh）是 input 的子串。
     * 例如：input="猪里脊肉"，可匹配 DB 中的"里脊肉"、"里脊"。
     * 按名称长度降序（越长越精确）。
     */
    @Query(value = """
            SELECT * FROM food
            WHERE is_active = 1 AND LOCATE(name_zh, :input) > 0
            ORDER BY LENGTH(name_zh) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Food> findWhereNameContainedIn(@Param("input") String input);

    /**
     * 正向子串匹配：input 是食物名称（name_zh）的子串。
     * 例如：input="里脊"，可匹配 DB 中的"猪里脊"、"牛里脊"。
     * 按名称长度升序（越短越精确）。
     */
    @Query(value = """
            SELECT * FROM food
            WHERE is_active = 1 AND name_zh LIKE CONCAT('%', :name, '%')
            ORDER BY LENGTH(name_zh) ASC
            LIMIT 5
            """, nativeQuery = true)
    List<Food> findByNameZhContaining(@Param("name") String name);
}
