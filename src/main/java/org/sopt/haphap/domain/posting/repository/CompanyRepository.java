package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.dto.CompanyAutocompleteProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query(value = """
            SELECT c.id AS id, c.name AS name
            FROM company c
            WHERE c.name ILIKE CONCAT('%', :keyword, '%')
            ORDER BY similarity(c.name, :keyword) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<CompanyAutocompleteProjection> searchByNameContaining(
            @Param("keyword") String keyword, @Param("limit") int limit);
}