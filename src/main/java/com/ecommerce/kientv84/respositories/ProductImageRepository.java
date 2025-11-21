package com.ecommerce.kientv84.respositories;

import com.ecommerce.kientv84.entites.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {
    @Query("SELECT MAX(pi.sortOrder) FROM ProductImageEntity pi WHERE pi.product.id = :productId")
    Integer findMaxSortOrderByProductId(UUID productId);

    // Lấy danh sách ảnh theo productId và sortOrders
    List<ProductImageEntity> findByProductIdAndSortOrderIn(UUID productId, List<Integer> sortOrders);
}
