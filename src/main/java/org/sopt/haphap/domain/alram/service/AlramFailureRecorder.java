package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramFailure;
import org.sopt.haphap.domain.alram.repository.AlramFailureRepository;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlramFailureRecorder {

    private final AlramFailureRepository alramFailureRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(RegistrationCreatedEvent event, Throwable e) {
        alramFailureRepository.save(AlramFailure.from(
                event.postingId(), event.registrantUserId(), event.stage(), e));
    }
}