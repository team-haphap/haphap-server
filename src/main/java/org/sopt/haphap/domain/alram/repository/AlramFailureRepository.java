package org.sopt.haphap.domain.alram.repository;

import java.util.List;
import org.sopt.haphap.domain.alram.domain.AlramFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlramFailureRepository extends JpaRepository<AlramFailure, Long> {
    // 재처리 및 모니터링을 위한 미해결 실패 조회
    List<AlramFailure> findByResolvedFalse();
}
