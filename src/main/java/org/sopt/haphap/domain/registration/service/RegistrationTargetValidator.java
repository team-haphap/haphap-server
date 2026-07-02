package org.sopt.haphap.domain.registration.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegistrationTargetValidator {

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;

    @Transactional(readOnly = true)
    public RegistrationTarget validate(Long userId, Long postingId, Long stageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.USER_NOT_FOUND));
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.POSTING_NOT_FOUND));
        PostingStage stage = postingStageRepository.findById(stageId)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.STAGE_NOT_FOUND));

        if (!stage.belongsTo(posting)) {
            throw new CustomException(RegistrationErrorCode.STAGE_NOT_IN_POSTING);
        }

        return new RegistrationTarget(user, posting, stage);
    }

    public record RegistrationTarget(User user, Posting posting, PostingStage stage) {}
}