package com.nutrivista.repository;

import com.nutrivista.entity.UserFavorite;
import com.nutrivista.entity.UserFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavoriteId> {

    @Query("SELECT uf FROM UserFavorite uf JOIN FETCH uf.food f WHERE uf.user.id = :userId ORDER BY uf.createdAt DESC")
    List<UserFavorite> findByUserId(@Param("userId") Long userId);

    boolean existsByIdUserIdAndIdFoodId(Long userId, Long foodId);
}
