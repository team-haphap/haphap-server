package org.sopt.haphap.domain.posting.service.support;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.service.calculator.NextStageCalculator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostingResponseAssembler {

    private final NextStageCalculator nextStageCalculator;

    public Scored assemble(Posting posting,
                           List<PostingStageFlatProjection> stages,
                           Map<Long, Long> counts,
                           String companyImageUrl) {

        var display = nextStageCalculator.resolveDisplay(stages, counts);
        var stage = display.stage();
        LocalDate announceDate = (stage == null) ? null : stage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(), posting.getTitle(),
                posting.getCompany().getName(), posting.getCategory().getName(),
                stage == null ? null : stage.getName(),
                display.label(),
                companyImageUrl);

        return new Scored(response, posting.getTitle(), announceDate,
                posting.getDeadline(), display.closed());

    }

    public record Scored(PopularPostingResponse response, String title,
                         LocalDate announceDate, LocalDate deadline,
                         boolean closed) {}
}