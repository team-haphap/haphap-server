package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingResponseAssembler {

    private final NextStageCalculator nextStageCalculator;

    /** 공고 + 전형 + 누적등록수 → 응답 DTO와 정렬키(nextStage 발표일)를 함께 담은 Scored */
    public Scored assemble(Posting posting,
                           List<PostingStageFlatProjection> stages,
                           Map<Long, Long> counts) {
        PostingStageFlatProjection nextStage = nextStageCalculator.calculate(stages, counts);
        Integer days = nextStageCalculator.daysUntil(nextStage);
        LocalDate announceDate = (nextStage == null) ? null : nextStage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(), posting.getTitle(),
                posting.getCompany().getName(), posting.getCategory().getName(),
                posting.getCompany().getDescription(),
                nextStage == null ? null : nextStage.getName(),
                days, posting.getCompany().getImageUrl());

        return new Scored(response, posting.getTitle(), announceDate, posting.getDeadline());
    }

    public record Scored(PopularPostingResponse response, String title, LocalDate announceDate, LocalDate deadline) {}
}