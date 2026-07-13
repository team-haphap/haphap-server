package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    long countByNameIn(List<String> names);
    List<Category> findByNameIn(List<String> names);
    Optional<Category> findByName(String name);
    boolean existsByName (String name);
}