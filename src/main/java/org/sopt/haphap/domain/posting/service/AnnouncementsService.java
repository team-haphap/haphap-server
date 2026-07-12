package org.sopt.haphap.domain.posting.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.dto.response.TodayAnnouncementPostingListResponse;
import org.sopt.haphap.domain.posting.dto.response.TodayAnnouncementPostingResponse;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementsService {

    private static final int LIMIT = 3;

    private final PostingStageRepository postingStageRepository;

    public TodayAnnouncementPostingListResponse getTodayAnnouncementPostings() {
        List<TodayAnnouncementPostingResponse> postings = postingStageRepository
                .findTodayAnnouncements(LocalDate.now(), CompanyImageType.TODAY_LOGO, PageRequest.of(0, LIMIT))
                .stream()
                .map(TodayAnnouncementPostingResponse::from)
                .toList();

        return TodayAnnouncementPostingListResponse.from(postings);
    }
}