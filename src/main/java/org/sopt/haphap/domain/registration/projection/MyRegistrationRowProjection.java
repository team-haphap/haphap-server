package org.sopt.haphap.domain.registration.projection;

import java.time.LocalDateTime;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;

// 유저의 등록 한 건 = (공고, 전형, 결과). 공고별 마지막 활동 시각과 "전형 순서상 가장 뒤 결과"를 서비스에서 함께 뽑는다.
public interface MyRegistrationRowProjection {
    Long getPostingId();
    String getStageName();
    int getStageOrder();
    RegistrationResult getResult();
    LocalDateTime getUpdatedAt();
}
