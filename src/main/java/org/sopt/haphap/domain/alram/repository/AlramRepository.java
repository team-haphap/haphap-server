package org.sopt.haphap.domain.alram.repository;

import org.sopt.haphap.domain.alram.domain.Alram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlramRepository extends JpaRepository<Alram, Long> {
}