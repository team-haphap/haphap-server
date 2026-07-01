package org.sopt.haphap.domain.user.repository;

import org.sopt.haphap.domain.user.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
}