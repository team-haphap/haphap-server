package org.sopt.haphap.domain.posting.repository;

import org.sopt.haphap.domain.posting.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}