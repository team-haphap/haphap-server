package org.sopt.haphap.domain.registration.repository;

import java.util.List;
import org.sopt.haphap.domain.registration.domain.RegistrationReview;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewReason;
import org.sopt.haphap.domain.registration.domain.RegistrationReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationReviewRepository extends JpaRepository<RegistrationReview, Long> {

    List<RegistrationReview> findByStatusOrderByCreatedAtAsc(RegistrationReviewStatus status);

    // 같은 사유로 이미 대기 중인 검토가 있으면 중복 생성 방지
    boolean existsByRegistrationIdAndReasonAndStatus(
            Long registrationId, RegistrationReviewReason reason, RegistrationReviewStatus status);

    // 이 등록건의 집계 보류 사유(SUBSEQUENT_STAGE_FAIL)가 아직 승인 안 됐는지.
    // REPEATED_FAILURE는 집계와 무관한 순수 알림이라 여기서 안 본다.
    boolean existsByRegistrationIdAndReasonAndStatusNot(
            Long registrationId, RegistrationReviewReason reason, RegistrationReviewStatus status);
}
