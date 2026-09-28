package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.repository.AlramRepository;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.user.service.withdrawal.WithdrawalCleaner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlramWithdrawalCleaner implements WithdrawalCleaner {

    private final PushTokenRepository pushTokenRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final AlramRepository alramRepository;

    @Override
    public void clean(Long userId) {
        pushTokenRepository.deleteAllByUserId(userId);
        alramSettingRepository.deleteAllByUserId(userId);
        alramRepository.deleteAllByUserId(userId);
    }
}

//탈퇴 서비스는 List<WithdrawalCleaner> 만 주입받도록..