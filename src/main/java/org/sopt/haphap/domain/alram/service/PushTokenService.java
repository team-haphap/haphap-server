package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;

    @Transactional
    public void deactivate(Long tokenId) {
        pushTokenRepository.findById(tokenId).ifPresent(PushToken::deactivate);
    }
}