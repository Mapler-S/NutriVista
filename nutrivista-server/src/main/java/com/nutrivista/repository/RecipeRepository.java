package com.nutrivista.repository;

import com.nutrivista.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    /** 批量查询已存在的 id，用于增量导入时跳过已入库菜谱 */
    Set<Recipe> findAllByIdIn(Iterable<String> ids);

    /**
     * 关键词 + 菜系 + 分类 组合搜索（LIKE，忽略大小写）。
     * 任意参数为 null 时该条件不生效。
     */
    @Query("""
            SELECT r FROM Recipe r
            WHERE (:keyword IS NULL OR r.name LIKE CONCAT('%', :keyword, '%'))
              AND (:cuisine  IS NULL OR r.cuisine  = :cuisine)
              AND (:category IS NULL OR r.category = :category)
            ORDER BY r.name
            """)
    Page<Recipe> search(@Param("keyword")  String keyword,
                        @Param("cuisine")  String cuisine,
                        @Param("category") String category,
                        Pageable pageable);

    /**
     * 统计各菜系菜谱数量，按数量降序。
     * 返回 Object[]{cuisine, count} 数组列表。
     */
    @Query("SELECT r.cuisine, COUNT(r) FROM Recipe r GROUP BY r.cuisine ORDER BY COUNT(r) DESC")
    List<Object[]> countByCuisine();
}
