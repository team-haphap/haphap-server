package org.sopt.haphap.domain.passcard.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.passcard.code.PassCardErrorCode;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveItemResponse;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveListResponse;
import org.sopt.haphap.domain.passcard.dto.response.UnconfirmedPassCardListResponse;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationVerificationStatus;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.global.exception.CustomException;
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
    public PassCardArchiveListResponse getArchive(Long userId) {
        List<PassCardArchiveItemResponse> items = registrationRepository
                .findConfirmedPassCards(userId, APPROVED).stream()
                .map(PassCardArchiveItemResponse::from)
                .toList();
        return PassCardArchiveListResponse.from(items);
    }

    /* 푸시 탭 / 보관함 [다시 보기]*/
    public PassCardResponse getPassCard(Long userId, Long registrationId) {
        Registration registration = registrationRepository
                .findPassCard(registrationId, userId, APPROVED)
                .orElseThrow(() -> new CustomException(PassCardErrorCode.PASS_CARD_NOT_FOUND));
        return PassCardResponse.from(registration);
    }

    /* 카드가 화면에 정상 노출된 시점에 호출 */
    @Transactional
    public void confirm(Long userId, Long registrationId) {
        Registration registration = registrationRepository
                .findByIdAndUser_IdAndVerificationStatus(registrationId, userId, APPROVED)
                .orElseThrow(() -> new CustomException(PassCardErrorCode.PASS_CARD_NOT_FOUND));
        registration.confirmPassCard();
    }
}