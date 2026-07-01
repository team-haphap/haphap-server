package org.sopt.haphap.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.AgreementSubmitRequest;
import org.sopt.haphap.domain.user.entity.Agreement;
import org.sopt.haphap.domain.user.entity.AgreementType;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.AgreementRepository;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementRepository agreementRepository;

    @Transactional
    public void saveAgreements(User user, AgreementSubmitRequest request) {
        Map<AgreementType, Boolean> answers = Map.of(
                AgreementType.PRIVACY_POLICY, request.privacyPolicyAgreed(),
                AgreementType.LOCATION_TERMS, request.locationTermsAgreed(),
                AgreementType.AGE_OVER_14, request.ageOver14Agreed(),
                AgreementType.CHANNEL_ADD, request.channelAddAgreed(),
                AgreementType.MARKETING, request.marketingAgreed()
        );

        boolean requiredAllAgreed = answers.entrySet().stream()
                .filter(e -> e.getKey().isRequired())
                .allMatch(Map.Entry::getValue);

        if (!requiredAllAgreed) {
            throw new CustomException(AuthErrorCode.REQUIRED_AGREEMENT_NOT_AGREED);
        }

        answers.forEach((type, agreed) ->
                agreementRepository.save(Agreement.create(user, type, agreed)));
    }
}