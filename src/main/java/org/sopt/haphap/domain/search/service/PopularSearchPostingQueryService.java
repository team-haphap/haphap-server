package org.sopt.haphap.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingListResponse;
import org.sopt.haphap.domain.search.dto.PopularSearchPostingResponse;
import org.sopt.haphap.domain.search.repository.PopularSearchPostingCacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularSearchPostingQueryService {

    private final PopularSearchPostingCacheRepository cacheRepository;

    public PopularSearchPostingListResponse getPopularPostings() {
        List<PopularSearchPostingResponse> postings = cacheRepository.find();
        return PopularSearchPostingListResponse.from(postings);
    }
}

//"집계"와 "서빙"의 책임을 분리했어요