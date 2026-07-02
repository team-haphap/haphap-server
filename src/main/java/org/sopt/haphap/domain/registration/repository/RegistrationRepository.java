package org.sopt.haphap.domain.registration.repository;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndPostingIdAndStageId(Long userId, Long postingId, Long stageId);

    //2-3(공고, 전형)별 등록 수를 한 번에
    @Query("""
        SELECT r.posting.id AS postingId, r.stage.id AS stageId, COUNT(r) AS cnt
        FROM Registration r
        WHERE r.posting.id IN :postingIds
        GROUP BY r.posting.id, r.stage.id
        """)
    List<StageRegistrationCountProjection> countByPostingAndStage(
            @Param("postingIds") List<Long> postingIds);
}