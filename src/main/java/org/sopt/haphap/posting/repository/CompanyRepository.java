package org.sopt.haphap.posting.repository;

import org.sopt.haphap.posting.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}