package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateRequest;
import org.sopt.haphap.domain.registration.dto.RegistrationCreateResponse;
import org.sopt.haphap.domain.registration.event.RegistrationCreatedEvent;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final RegistrationRepository registrationRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RegistrationCreateResponse createRegistration(Long userId, RegistrationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.USER_NOT_FOUND));
        Posting posting = postingRepository.findById(request.postingId())
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.POSTING_NOT_FOUND));

        Registration registration = Registration.create(
                user, posting, request.stage(), request.result(),
                request.contactMethod(), request.contactedAt(), request.anonymous());
        registrationRepository.save(registration);

        applyAlramSetting(user, posting, request.alarmEnabled());

        // 등록이 커밋된 뒤 알람 모듈이 이 이벤트를 받아 처리.
        eventPublisher.publishEvent(new RegistrationCreatedEvent(
                registration.getId(), posting.getId(), registration.getStage(), user.getId()));

        return RegistrationCreateResponse.from(registration.getId());
    }

    // (member, posting) 당 알람설정은 1개. 있으면 토글, 없으면 생성.
    // 이걸로 할지 아니면 분리??
    private void applyAlramSetting(User user, Posting posting, boolean enabled) {
        alramSettingRepository.findByUserIdAndPostingId(user.getId(), posting.getId())
                .ifPresentOrElse(
                        setting -> setting.updateEnabled(enabled),
                        () -> alramSettingRepository.save(AlramSetting.create(user, posting, enabled))
                );
    }
}