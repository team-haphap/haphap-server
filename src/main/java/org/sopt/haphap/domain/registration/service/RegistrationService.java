package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.service.AlramSettingService;
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
    private final ApplicationEventPublisher eventPublisher;
    private final RegistrationTargetValidator registrationTargetValidator;
    private final AlramSettingService alramSettingService;

    @Transactional
    public RegistrationCreateResponse createRegistration(Long userId, RegistrationCreateRequest request) {
        validateResultConsistency(request);

        RegistrationTargetValidator.RegistrationTarget target =
                registrationTargetValidator.validate(userId, request.postingId(), request.stageId());


        Optional<Registration> existing = registrationRepository
                .findByUserIdAndPostingIdAndStageId(userId, request.postingId(), request.stageId());

        Registration registration = existing
                .map(e -> updateExisting(e,target, request))       // 기존 있으면 갱신 or 막기
                .orElseGet(() -> createNew(target.user(), target.posting(), target.stage(), request));  // 없으면 신규

        alramSettingService.apply(target.user(), target.posting(), request.alarmEnabled());
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
        // 이미 확정인데 또 등록 시도 → 막기
        if (!existing.isPending()) {
            throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
        }

        // PENDING 상태에서 다시 PENDING으로는 갱신 불가 (PASS/FAIL 확정만 허용)
        if (request.result() == RegistrationResult.PENDING) {
            throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
        }

        // 여기까지 왔으면 PENDING → PASS/FAIL 확정
        existing.updateRegistration(request.result(), request.contactMethod(),
                request.contactedAt(), request.anonymous());
        eventPublisher.publishEvent(new RegistrationResultChangedEvent(
                target.posting().getId(), target.stage().getId(), request.result()));

        return existing;
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

    private void validateResultConsistency(RegistrationCreateRequest request) {
        boolean isPending = request.result() == RegistrationResult.PENDING;

        if (isPending) {
            // PENDING이면 연락 정보(수단·날짜)가 없어야 함.
            if (request.contactMethod() != null || request.contactedAt() != null) {
                throw new CustomException(RegistrationErrorCode.PENDING_MUST_NOT_HAVE_CONTACT);
            }
        } else {
            // 확정이면 연락 정보가 있어야 함
            if (request.contactMethod() == null || request.contactedAt() == null) {
                throw new CustomException(RegistrationErrorCode.CONFIRMED_MUST_HAVE_CONTACT);
            }
        }
    }
}