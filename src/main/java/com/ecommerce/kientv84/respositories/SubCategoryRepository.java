package com.ecommerce.kientv84.respositories;

import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.SubCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubCategoryRepository extends JpaRepository<SubCategoryEntity, UUID>, JpaSpecificationExecutor<SubCategoryEntity> {

    SubCategoryEntity findSubCategoryBySubCategoryCode(String code);

    @Query(value = """
        SELECT * FROM sub_category_entity
        WHERE document_tsv @@ to_tsquery('simple', :q || ':*')
        ORDER BY ts_rank(document_tsv, to_tsquery('simple', :q || ':*')) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SubCategoryEntity> searchSubCategorySuggestion(@Param("q") String q, @Param("limit") int limit);
}
