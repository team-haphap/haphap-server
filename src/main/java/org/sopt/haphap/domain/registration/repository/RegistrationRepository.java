package org.sopt.haphap.domain.registration.repository;

import org.sopt.haphap.domain.registration.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUserIdAndPostingIdAndStageId(Long userId, Long postingId, Long stageId);
}