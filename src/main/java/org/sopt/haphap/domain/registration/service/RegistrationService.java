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

import java.util.Optional;

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

        Optional<Registration> existing = registrationRepository
                .findByUserIdAndPostingIdAndStage(userId, request.postingId(), request.stage());

        if (existing.isPresent()) {
            return handleExisting(existing.get(), request, posting, user);
        }
        return createNew(user, posting, request);

    }
    private RegistrationCreateResponse handleExisting(Registration existing,
                                                      RegistrationCreateRequest request, Posting posting, User user) {

        // 결과까지 완전히 동일
        if (existing.hasSameResult(request.result())) {
            throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
        }

        // 기존이 대기 상태 → 변경 가능
        if (existing.isPending()) {
            if (!request.force()) {
                // 아직 확인 안 받음 → 모달 띄우라고 신호만
                return RegistrationCreateResponse.confirmRequired(existing.getId());
            }
            // force=true → 실제로 갱신
            existing.updateRegistration(request.result(), request.contactMethod(),
                    request.contactedAt(), request.anonymous());
            applyAlramSetting(user, posting, request.alarmEnabled());
            // 변경도 '새 전형 소식'이므로 구독자에게 알람
            publishEvent(existing, posting, user);
            return RegistrationCreateResponse.updated(existing.getId());
        }

        // 기존이 이미 확정(PASS/FAIL)인데 다른 결과가 들어온 경우
        throw new CustomException(RegistrationErrorCode.DUPLICATE_REGISTRATION);
    }
    private RegistrationCreateResponse createNew(User user, Posting posting,
                                                 RegistrationCreateRequest request) {
        Registration registration = Registration.create(
                user, posting, request.stage(), request.result(),
                request.contactMethod(), request.contactedAt(), request.anonymous());
        registrationRepository.save(registration);
        applyAlramSetting(user, posting, request.alarmEnabled());
        publishEvent(registration, posting, user);
        return RegistrationCreateResponse.created(registration.getId());
    }
    private void publishEvent(Registration registration, Posting posting, User user) {
        eventPublisher.publishEvent(new RegistrationCreatedEvent(
                registration.getId(), posting.getId(), registration.getStage(), user.getId()));
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