package org.sopt.haphap.domain.posting.service.support;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingResponseAssembler {

    private final NextStageCalculator nextStageCalculator;

    public Scored assemble(Posting posting,
                           List<PostingStageFlatProjection> stages,
                           Map<Long, Long> counts,
                           String companyImageUrl) {
        PostingStageFlatProjection nextStage = nextStageCalculator.calculate(stages, counts);
        Integer days = nextStageCalculator.daysUntil(nextStage);
        LocalDate announceDate = (nextStage == null) ? null : nextStage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(), posting.getTitle(),
                posting.getCompany().getName(), posting.getCategory().getName(),
                nextStage == null ? null : nextStage.getName(),
                days, companyImageUrl);

        return new Scored(response, posting.getTitle(), announceDate, posting.getDeadline());
    }

    public record Scored(PopularPostingResponse response, String title, LocalDate announceDate, LocalDate deadline) {}
}