package org.sopt.haphap.domain.passcard.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.passcard.code.PassCardErrorCode;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveItemResponse;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveListResponse;
import org.sopt.haphap.domain.passcard.dto.response.PassCardDetailResponse;
import org.sopt.haphap.domain.passcard.dto.response.PassCardStageOptionResponse;
import org.sopt.haphap.domain.passcard.dto.response.UnconfirmedPassCardListResponse;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassCardService {

    private static final RegistrationVerificationStatus APPROVED = RegistrationVerificationStatus.APPROVED;

    private final RegistrationRepository registrationRepository;

    /* 앱 실행 시: 아직 확인하지 않은 승인된 합격카드 */
    public UnconfirmedPassCardListResponse getUnconfirmedPassCards(Long userId) {
        List<PassCardResponse> cards = registrationRepository
                .findUnconfirmedPassCards(userId, APPROVED).stream()
                .map(PassCardResponse::from)
                .toList();
        return UnconfirmedPassCardListResponse.from(cards);
    }

    /* 마이페이지 > 합격 카드 보관함 */
    public PassCardArchiveListResponse getArchive(Long userId, Pageable pageable) {
        Page<PassCardArchiveItemResponse> page = registrationRepository
                .findConfirmedPassCards(userId, APPROVED, pageable)
                .map(PassCardArchiveItemResponse::from);
        return PassCardArchiveListResponse.from(page);
    }

    /* 푸시 탭 / 보관함 [다시 보기]*/
    public PassCardDetailResponse getPassCard(Long userId, Long registrationId) {
        Registration registration = registrationRepository
                .findPassCard(registrationId, userId, APPROVED)
                .orElseThrow(() -> new CustomException(PassCardErrorCode.PASS_CARD_NOT_FOUND));
        List<PassCardStageOptionResponse> availableStages = registrationRepository
                .findApprovedPassCardsByPosting(userId, registration.getPosting().getId(), APPROVED)
                .stream()
                .map(PassCardStageOptionResponse::from)
                .toList();
        return PassCardDetailResponse.from(registration, availableStages);
    }

    /* 카드가 화면에 정상 노출된 시점에 호출 */
    @Transactional
    public void confirm(Long userId, Long registrationId) {
        int updated = registrationRepository.markPassCardConfirmed(
                registrationId, userId, APPROVED, LocalDateTime.now());

        // 0건 = 이미 확인됨 or 내 승인 카드가 아님(404)
        if (updated == 0 && !registrationRepository
                .existsByIdAndUser_IdAndVerificationStatus(registrationId, userId, APPROVED)) {
            throw new CustomException(PassCardErrorCode.PASS_CARD_NOT_FOUND);
        }
    }
}
