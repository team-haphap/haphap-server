package org.sopt.haphap.domain.posting.service.support;

import java.time.LocalDate;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.dto.response.PopularPostingResponse;
import org.springframework.stereotype.Component;

@Component
public class PostingResponseAssembler {

    public Scored assemble(Posting posting, String companyImageUrl) {

        var display = posting.resolveDisplay();
        PostingStage stage = display.stage();
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
