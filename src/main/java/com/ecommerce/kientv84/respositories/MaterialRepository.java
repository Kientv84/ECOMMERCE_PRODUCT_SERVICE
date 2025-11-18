package com.ecommerce.kientv84.respositories;

import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.CategoryEntity;
import com.ecommerce.kientv84.entites.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID>, JpaSpecificationExecutor<MaterialEntity> {
    MaterialEntity findMaterialByMaterialCode(String materialCode);

    @Query(value = """
        SELECT * FROM material_entity
        WHERE document_tsv @@ to_tsquery('simple', :q || ':*')
        ORDER BY ts_rank(document_tsv, to_tsquery('simple', :q || ':*')) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MaterialEntity> searchMaterialSuggestion(@Param("q") String q, @Param("limit") int limit);
}
