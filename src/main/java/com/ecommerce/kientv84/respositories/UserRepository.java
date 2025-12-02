package com.ecommerce.kientv84.respositories;

import com.ecommerce.kientv84.entites.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    @Query(value = """
    SELECT * FROM user_entity
    WHERE document_tsv @@ to_tsquery('simple', unaccent(lower(:q)) || ':*')
    ORDER BY ts_rank(document_tsv, to_tsquery('simple', unaccent(lower(:q)) || ':*')) DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<UserEntity> searchUserSuggestion(@Param("q") String q, @Param("limit") int limit);

    // Hoặc dùng function PostgreSQL đã tạo
    @Query(value = "SELECT * FROM user_search_suggest(:q, :limit)", nativeQuery = true)
    List<UserEntity> searchUserSuggestionFunction(@Param("q") String q, @Param("limit") int limit);

    UserEntity findByUserEmail(String email);
//
//    List<UserEntity> findByRole_Id(Long id); //structure findBy + [Tên trường quan hệ trong Entity] + _ + [Tên trường trong Entity liên quan]
}
