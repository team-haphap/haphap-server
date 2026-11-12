package org.sopt.haphap.domain.search.keyword;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.search.code.SearchErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchExecutionService {

    private static final int MAX_KEYWORD_LENGTH = 255;

    private final SearchKeywordLogRepository repository;

    @Transactional
    public void record(String rawKeyword) {
        String keyword = rawKeyword == null ? "" : rawKeyword.strip();
        if (keyword.isEmpty()) {
            throw new CustomException(SearchErrorCode.KEYWORD_REQUIRED);
        }
        if (keyword.length() > MAX_KEYWORD_LENGTH) {
            keyword = keyword.substring(0, MAX_KEYWORD_LENGTH);
        }
        repository.save(SearchKeywordLog.of(keyword));
    }
}