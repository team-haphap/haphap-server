package org.sopt.haphap.domain.home.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.home.domain.PostingViewHistory;
import org.sopt.haphap.domain.home.repository.PostingViewHistoryRepository;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 공고 상세페이지에 정상 진입했을 때 "최근 조회한 공고"에 기록. 같은 (유저,공고)면 시각만 갱신(upsert).
// 호출부(PostingDetailService)가 readOnly 트랜잭션이라 REQUIRES_NEW로 별도 쓰기 트랜잭션을 연다.
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
                        () -> postingViewHistoryRepository.save(PostingViewHistory.create(
                                userRepository.getReferenceById(userId), posting, now)));
    }
}
