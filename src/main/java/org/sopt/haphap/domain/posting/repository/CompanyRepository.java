package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    boolean existsByName (String name);
}