package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.domain.AlramSetting;
import org.sopt.haphap.domain.alram.repository.AlramSettingRepository;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.event.RegistrationResultChangedEvent;
import org.sopt.haphap.domain.registration.event.StageResultCountedEvent;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.posting.domain.Posting;
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

    private final RegistrationRepository registrationRepository;
    private final AlramSettingRepository alramSettingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RegistrationTargetValidator registrationTargetValidator;

    @Transactional
    public RegistrationCreateResponse createRegistration(Long userId, RegistrationCreateRequest request) {

        RegistrationTargetValidator.RegistrationTarget target =
                registrationTargetValidator.validate(userId, request.postingId(), request.stageId());


        Optional<Registration> existing = registrationRepository
                .findByUserIdAndPostingIdAndStageId(userId, request.postingId(), request.stageId());

        Registration registration = existing
                .map(e -> updateExisting(e,target, request))       // 기존 있으면 갱신 or 막기
                .orElseGet(() -> createNew(target.user(), target.posting(), target.stage(), request));  // 없으면 신규

        applyAlramSetting(target.user(), target.posting(), request.alarmEnabled());
        publishEvent(registration, target.posting(), target.user());

        // PASS일 때만 연관을 fetch join으로 당겨와 카드 정보 구성, 아니면 ID만
        if (registration.isPass()) {
            Registration detailed = registrationRepository.findByIdWithDetails(registration.getId())
                    .orElseThrow(() -> new CustomException(RegistrationErrorCode.REGISTRATION_NOT_FOUND));
            return RegistrationCreateResponse.pass(detailed);
        }
        return RegistrationCreateResponse.idOnly(registration.getId());

    }

    private Registration updateExisting(Registration existing,
                                        RegistrationTargetValidator.RegistrationTarget target,
                                        RegistrationCreateRequest request) {
        // 대기 상태였으면 갱신 허용 (확인 API에서 CONFIRM 받고 온 케이스)
        if (existing.isPending()) {
            existing.updateRegistration(request.result(), request.contactMethod(),
                    request.contactedAt(), request.anonymous());
            // PENDING → 확정으로 바뀐 경우만 집계 이동
            if (request.result() != RegistrationResult.PENDING) {
                eventPublisher.publishEvent(new RegistrationResultChangedEvent(
                        target.posting().getId(), target.stage().getId(), request.result()));
            }

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
        registrationRepository.save(registration);
        // 집계 신규 이벤트만
        eventPublisher.publishEvent(new StageResultCountedEvent(
                posting.getId(), stage.getId(), request.result()));

        return registration;
    }


    private void publishEvent(Registration registration, Posting posting, User user) {
        eventPublisher.publishEvent(new RegistrationCreatedEvent(
                registration.getId(), posting.getId(), registration.getStage().getName(), user.getId()));
    }

    // (member, posting) 당 알람설정은 1개. 있으면 토글, 없으면 생성.
    private void applyAlramSetting(User user, Posting posting, boolean enabled) {
        alramSettingRepository.findByUserIdAndPostingId(user.getId(), posting.getId())
                .ifPresentOrElse(
                        setting -> setting.updateEnabled(enabled),
                        () -> alramSettingRepository.save(AlramSetting.create(user, posting, enabled))
                );
    }
}