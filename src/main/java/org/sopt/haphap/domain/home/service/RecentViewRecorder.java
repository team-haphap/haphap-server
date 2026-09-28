package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.home.domain.PostingViewHistory;
import org.sopt.haphap.domain.home.repository.PostingViewHistoryRepository;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 공고 상세페이지에 정상 진입했을 때 "최근 조회한 공고"에 기록. 같은 (유저,공고)면 시각만 갱신(upsert).
// 호출부(PostingDetailService)가 readOnly 트랜잭션이라 REQUIRES_NEW로 별도 쓰기 트랜잭션을 연다.
@Slf4j
@Component
@RequiredArgsConstructor
public class RecentViewRecorder {

    private final PostingViewHistoryRepository postingViewHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, Posting posting) {
        LocalDateTime now = LocalDateTime.now();
        postingViewHistoryRepository.findByUserIdAndPostingId(userId, posting.getId())
                .ifPresentOrElse(
                        history -> history.touch(now),
                        () -> insertIgnoringConcurrentDuplicate(userId, posting, now));
    }

    // 같은 (유저,공고)에 대한 최초 조회가 동시에 들어오면 둘 다 findBy...에서 "없음"을 볼 수 있음(check-then-act).
    // 유니크 제약(uk_posting_view_history)에 걸린 쪽은 이미 다른 요청이 같은 순간 기록을 남긴 것이므로 무시.
    // save()는 flush 시점까지 INSERT가 미뤄질 수 있어 saveAndFlush로 즉시 실행해야 이 자리에서 예외를 잡을 수 있음.
    private void insertIgnoringConcurrentDuplicate(Long userId, Posting posting, LocalDateTime now) {
        try {
            postingViewHistoryRepository.saveAndFlush(PostingViewHistory.create(
                    userRepository.getReferenceById(userId), posting, now));
        } catch (DataIntegrityViolationException e) {
            log.debug("최근 조회 기록 동시 삽입 경합 - userId={}, postingId={}", userId, posting.getId());
        }
    }
}
