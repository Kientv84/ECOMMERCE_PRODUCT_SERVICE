package com.ecommerce.kientv84.respositories;

import com.ecommerce.kientv84.entites.BrandEntity;
import com.ecommerce.kientv84.entites.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {
    @Query(value = """
        SELECT * FROM role_entity
        WHERE document_tsv @@ to_tsquery('simple', :q || ':*')
        ORDER BY ts_rank(document_tsv, to_tsquery('simple', :q || ':*')) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<RoleEntity> searchRoleSuggestion(@Param("q") String q, @Param("limit") int limit);
}
