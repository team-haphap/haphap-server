package org.sopt.haphap.domain.alram.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlramSettingService {

    private final AlramSettingRepository alramSettingRepository;
    private final UserRepository userRepository;
    private final PostingRepository postingRepository;

    @Transactional
    public void setAlrams(Long userId, Long postingId) {
        applyByIds(userId, postingId, true);
    }

    @Transactional
    public void deleteAlrams(Long userId, Long postingId) {
        applyByIds(userId, postingId, false);
    }

    private void applyByIds(Long userId, Long postingId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.USER_NOT_FOUND));
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.POSTING_NOT_FOUND));
        apply(user, posting, enabled);
    }

    @Transactional
    // (member, posting) 당 알람설정은 1개. 있으면 토글, 없으면 생성.
    public void apply(User user, Posting posting, boolean enabled) {
        alramSettingRepository.findByUserIdAndPostingId(user.getId(), posting.getId())
                .ifPresentOrElse(
                        setting -> setting.updateEnabled(enabled),
                        () -> alramSettingRepository.save(AlramSetting.create(user, posting, enabled))
                );
    }
}
