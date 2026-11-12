package org.sopt.haphap.domain.content.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.content.domain.Content;
import org.sopt.haphap.domain.content.dto.request.ContentCreateRequest;
import org.sopt.haphap.domain.content.dto.response.ContentCreateResponse;
import org.sopt.haphap.domain.content.repository.ContentRepository;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.UserService;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final PostingRepository postingRepository;
    private final UserService userService;

    @Transactional
    public ContentCreateResponse create(Long userId, ContentCreateRequest request) {
        User user = userService.findById(userId);
        Posting posting = resolvePosting(request.postingId());

        Content content = Content.create(
                user, posting, request.type(),
                request.title().strip(), request.body().strip());
        contentRepository.save(content);

        return ContentCreateResponse.from(content);
    }

    private Posting resolvePosting(Long postingId) {
        if (postingId == null) {
            return null;
        }
        return postingRepository.findById(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));
    }
}