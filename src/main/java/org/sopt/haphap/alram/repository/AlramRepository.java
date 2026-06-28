package org.sopt.haphap.alram.repository;

import org.sopt.haphap.alram.domain.Alram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlramRepository extends JpaRepository<Alram, Long> {
}