package org.sopt.haphap.posting.repository;

import org.sopt.haphap.posting.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}