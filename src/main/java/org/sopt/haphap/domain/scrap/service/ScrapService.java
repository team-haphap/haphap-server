package org.sopt.haphap.domain.scrap.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.scrap.dto.response.MyScrapListResponse;
import org.sopt.haphap.domain.scrap.dto.response.MyScrapResponse;
import org.sopt.haphap.domain.scrap.repository.ScrapRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostingRepository postingRepository;

    // 멱등: 이미 찜한 공고면 아무것도 바꾸지 않는다. 마감된 공고도 찜할 수 있다.
    // @return 이번 요청으로 새로 찜했으면 true, 이미 찜해 있었으면 false
    public boolean scrap(Long userId, Long postingId) {
        if (!postingRepository.existsById(postingId)) {
            throw new CustomException(PostingErrorCode.POSTING_NOT_FOUND);
        }
        return scrapRepository.insertIfAbsent(userId, postingId, LocalDateTime.now()) == 1;
    }

    // 찜하지 않은 공고를 취소해도 에러가 아니다. 단 공고 자체가 없으면 404.
    // 공고 존재 확인은 지운 게 없을 때만 한다 (찜이 있었다면 FK상 공고도 있으므로 일반 경로는 쿼리 1번).
    // @return 이번 요청으로 찜을 취소했으면 true, 원래 찜하지 않았으면 false
    public boolean unscrap(Long userId, Long postingId) {
        if (scrapRepository.deleteByUserIdAndPostingId(userId, postingId) == 1) {
            return true;
        }
        if (!postingRepository.existsById(postingId)) {
            throw new CustomException(PostingErrorCode.POSTING_NOT_FOUND);
        }
        return false;
    }

    // 마이페이지 [찜한 공고]: 찜한 시각 최신순(같으면 공고 id 역순). 페이지네이션 없이 전부, 쿼리 1번.
    @Transactional(readOnly = true)
    public MyScrapListResponse getMyScraps(Long userId) {
        List<MyScrapResponse> scraps = scrapRepository.findMyScraps(userId, CompanyImageType.CALENDAR_LOGO).stream()
                .map(MyScrapResponse::from)
                .toList();
        return MyScrapListResponse.from(scraps);
    }
}
