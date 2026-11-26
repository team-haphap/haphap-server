package org.sopt.haphap.domain.posting.service;
/*
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.domain.StageType;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.service.RegistrationQueryService;

@ExtendWith(MockitoExtension.class)
class TodayStatisticServiceTest {

    @Mock
    private RegistrationQueryService registrationQueryService;
    @Mock
    private PostingStageRepository postingStageRepository;
    @Mock
    private PostingRepository postingRepository;

    private TodayStatisticService sut;

    @BeforeEach
    void setUp() {
        sut = new TodayStatisticService(registrationQueryService, postingStageRepository, postingRepository);
    }

    private Posting openPosting() {
        Posting posting = Posting.create("공고", LocalDate.now().plusDays(30), "서울", "백엔드", null, null);
        PostingStage current = PostingStage.create("서류", 1, null, 0, StageType.DOCUMENT, posting);
        posting.moveCurrentStageTo(current);
        return posting;
    }

    private Posting closedPosting() {
        Posting posting = Posting.create("공고", LocalDate.now().plusDays(30), "서울", "백엔드", null, null);
        PostingStage finalStage = PostingStage.create("최종합격", 5, null, 0, StageType.FINAL_PASS, posting);
        finalStage.markMoved(LocalDateTime.now().minusDays(10));
        posting.moveCurrentStageTo(finalStage);
        return posting;
    }

    @Test
    void 전형이_있는_공고가_없으면_온고잉_0이다() {
        when(postingStageRepository.findDistinctPostingIds()).thenReturn(List.of());
        when(registrationQueryService.countTodayEvents(any(), any())).thenReturn(0L);
        when(postingStageRepository.countPostingsAnnouncedToday(any())).thenReturn(0L);

        var response = sut.getTodayStatistics();

        assertThat(response.onGoingCount()).isEqualTo(0);
    }

    @Test
    void 마감되지_않은_공고만_온고잉으로_센다() {
        when(postingStageRepository.findDistinctPostingIds()).thenReturn(List.of(1L, 2L, 3L));
        when(postingRepository.findAllWithCurrentStageByIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(openPosting(), openPosting(), closedPosting()));
        when(registrationQueryService.countTodayEvents(any(), any())).thenReturn(5L);
        when(postingStageRepository.countPostingsAnnouncedToday(any())).thenReturn(2L);

        var response = sut.getTodayStatistics();

        assertThat(response.onGoingCount()).isEqualTo(2);
        assertThat(response.cumulatedCount()).isEqualTo(5);
        assertThat(response.announcedCount()).isEqualTo(2);
    }
}

 */
