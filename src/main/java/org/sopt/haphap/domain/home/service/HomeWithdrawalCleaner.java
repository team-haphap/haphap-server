package org.sopt.haphap.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.repository.PostingViewHistoryRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HomeWithdrawalCleaner implements WithdrawalCleaner {

    private final PostingViewHistoryRepository postingViewHistoryRepository;

    @Override
    public void clean(Long userId) {
        postingViewHistoryRepository.deleteAllByUserId(userId);   // 정책: 저장한 공고 / 조회 이력 삭제
    }
}