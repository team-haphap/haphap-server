package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RegistrationCreateResponse createRegistration(Long userId, RegistrationCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.USER_NOT_FOUND));
        Posting posting = postingRepository.findById(request.postingId())
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.POSTING_NOT_FOUND));
        PostingStage stage = postingStageRepository.findById(request.stageId())
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.STAGE_NOT_FOUND));

        if (!stage.belongsTo(posting)) {
            throw new CustomException(RegistrationErrorCode.STAGE_NOT_IN_POSTING);
        }

        Optional<Registration> existing = registrationRepository
                .findByUserIdAndPostingIdAndStageId(userId, request.postingId(), request.stageId());

        Registration registration = existing
                .map(e -> updateExisting(e, request))       // 기존 있으면 갱신 or 막기
                .orElseGet(() -> createNew(user, posting, stage, request));  // 없으면 신규

        applyAlramSetting(user, posting, request.alarmEnabled());
        publishEvent(registration, posting, user);
        return RegistrationCreateResponse.from(registration.getId());

    }

    private Registration updateExisting(Registration existing, RegistrationCreateRequest request) {
        // 대기 상태였으면 갱신 허용 (확인 API에서 CONFIRM 받고 온 케이스)
        if (existing.isPending()) {
            existing.updateRegistration(request.result(), request.contactMethod(),
                    request.contactedAt(), request.anonymous());
            return existing;
        }
        // 이미 확정인데 또 등록 시도 → 막기
        throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
    }
    private Registration createNew(User user, Posting posting, PostingStage stage,
                                   RegistrationCreateRequest request) {
        Registration registration = Registration.create(
                user, posting, stage, request.result(),
                request.contactMethod(), request.contactedAt(), request.anonymous());
        return registrationRepository.save(registration);
    }


    private void publishEvent(Registration registration, Posting posting, User user) {
        eventPublisher.publishEvent(new RegistrationCreatedEvent(
                registration.getId(), posting.getId(), registration.getStage().getName(), user.getId()));
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