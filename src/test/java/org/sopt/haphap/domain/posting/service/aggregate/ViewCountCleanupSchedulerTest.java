package org.sopt.haphap.domain.posting.service.aggregate;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.sopt.haphap.domain.posting.service.PostingViewTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;
/*
@ExtendWith(MockitoExtension.class)
class ViewCountCleanupSchedulerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private PostingStageRepository postingStageRepository;
    @Mock
    private PostingRepository postingRepository;

    private ViewCountCleanupScheduler sut;

    @BeforeEach
    void setUp() {
        sut = new ViewCountCleanupScheduler(redisTemplate, postingStageRepository, postingRepository);
    }

    private Posting posting(long id, boolean closed) {
        Posting posting = Posting.create("공고", LocalDate.now().plusDays(30), "서울", "백엔드", null, null);
        ReflectionTestUtils.setField(posting, "id", id);
        StageType type = closed ? StageType.FINAL_PASS : StageType.DOCUMENT;
        PostingStage stage = PostingStage.create("전형", 1, null, 0, type, posting);
        if (closed) {
            stage.markMoved(LocalDateTime.now().minusDays(10));
        }
        posting.moveCurrentStageTo(stage);
        return posting;
    }

    @Test
    void 전형있는_공고가_없으면_레디스를_건드리지_않는다() {
        when(postingStageRepository.findDistinctPostingIds()).thenReturn(List.of());

        sut.removeClosedPostings();

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    void 마감된_공고가_없으면_레디스를_건드리지_않는다() {
        when(postingStageRepository.findDistinctPostingIds()).thenReturn(List.of(1L));
        when(postingRepository.findAllWithCurrentStageByIds(List.of(1L)))
                .thenReturn(List.of(posting(1L, false)));

        sut.removeClosedPostings();

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    void 마감된_공고_id만_레디스에서_제거한다() {
        when(postingStageRepository.findDistinctPostingIds()).thenReturn(List.of(1L, 2L));
        when(postingRepository.findAllWithCurrentStageByIds(List.of(1L, 2L)))
                .thenReturn(List.of(posting(1L, false), posting(2L, true)));
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        sut.removeClosedPostings();

        verify(zSetOperations).remove(PostingViewTracker.VIEW_COUNT_KEY, "2");
    }
}

 */
