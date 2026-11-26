package org.sopt.haphap.domain.content.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.content.code.ContentErrorCode;
import org.sopt.haphap.domain.content.domain.Content;
import org.sopt.haphap.domain.content.domain.ContentType;
import org.sopt.haphap.domain.content.dto.response.ContentDetailResponse;
import org.sopt.haphap.domain.content.dto.response.ContentListResponse;
import org.sopt.haphap.domain.content.repository.ContentRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentQueryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final ContentRepository contentRepository;

    /* 커뮤니티 탐색: 타입 미지정이면 전체, 최신순 */
    public ContentListResponse explore(ContentType type, Integer page, Integer size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        Pageable pageable = PageRequest.of(p, s);
        Slice<Content> slice = (type == null)
                ? contentRepository.findSliceAll(pageable)
                : contentRepository.findSliceByType(type, pageable);
        return ContentListResponse.of(slice, p, s);
    }

    /* 상세 열람 */
    public ContentDetailResponse getDetail(Long viewerId, Long contentId) {
        Content content = contentRepository.findDetailById(contentId)
                .orElseThrow(() -> new CustomException(ContentErrorCode.CONTENT_NOT_FOUND));
        return ContentDetailResponse.of(content, viewerId);
    }

    /* 마이페이지: 내가 작성한 콘텐츠 */
    public ContentListResponse getMyContents(Long userId, Integer page, Integer size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        return ContentListResponse.of(contentRepository.findSliceByUserId(userId, PageRequest.of(p, s)), p, s);
    }

    private int normalizePage(Integer page) {
        return (page == null || page < 0) ? 0 : page;
    }

    private int normalizeSize(Integer size) {
        return (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }
}