package org.sopt.haphap.domain.scrap.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.scrap.repository.ScrapRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapWithdrawalCleaner implements WithdrawalCleaner {

    private final ScrapRepository scrapRepository;

    @Override
    public void clean(Long userId) {
        scrapRepository.deleteAllByUserId(userId);   // 정책: 찜한 공고 삭제
    }
}
