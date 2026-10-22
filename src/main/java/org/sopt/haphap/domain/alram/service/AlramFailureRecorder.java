package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramFailure;
import org.sopt.haphap.domain.alram.repository.AlramFailureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlramFailureRecorder {

    private final AlramFailureRepository alramFailureRepository;

    // registrantUserId/stage는 발송 종류에 따라 없을 수 있다(예: 마감 알림은 등록자가 없음) - nullable.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long postingId, Long registrantUserId, String stage, Throwable e) {
        alramFailureRepository.save(AlramFailure.from(postingId, registrantUserId, stage, e));
    }
}
