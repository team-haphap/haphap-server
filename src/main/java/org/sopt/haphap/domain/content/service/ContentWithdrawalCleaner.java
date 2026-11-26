package org.sopt.haphap.domain.content.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.content.repository.ContentRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentWithdrawalCleaner implements WithdrawalCleaner {

    private final ContentRepository contentRepository;

    // 커뮤니티 글은 비식별 데이터로 유지 - 작성자 연결만 끊기
    @Override
    public void clean(Long userId) {
        contentRepository.detachAllByUserId(userId);
    }
}