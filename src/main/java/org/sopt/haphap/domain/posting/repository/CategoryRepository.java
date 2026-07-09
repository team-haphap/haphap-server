package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName (String name);
}