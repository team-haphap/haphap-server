package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;
import org.sopt.haphap.domain.posting.dto.response.PostingDetailResponse;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.repository.StageResultCountRepository;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.dto.RecentParticipantProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingDetailService {

    private static final int PROFILE_LIMIT = 4;
    private static final int FEED_LIMIT = 30;

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final StageResultCountRepository stageResultCountRepository;
    private final RegistrationRepository registrationRepository;
    private final NextStageCalculator nextStageCalculator;

    public PostingDetailResponse getDetail(Long postingId) {
        // 공고 + 회사 + 카테고리
        Posting posting = postingRepository.findWithCompanyAndCategory(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));

        // currentState 계산 (집계 테이블 재사용)
        String currentState = resolveCurrentState(postingId);

        // 요약
        long registeredCount = registrationRepository.countDistinctUsersByPostingId(postingId);
        List<String> profileImages = registrationRepository
                .findRecentParticipants(postingId, PageRequest.of(0, PROFILE_LIMIT))
                .stream()
                .map(RecentParticipantProjection::getProfileImageUrl)
                .toList();

        long additional = Math.max(0, registeredCount - PROFILE_LIMIT);

        PostingDetailResponse.SummaryResponse summary =
                new PostingDetailResponse.SummaryResponse(registeredCount, profileImages, additional);

        // 4) 실시간 제보 최근 30개
        List<PostingDetailResponse.RegistrationFeedResponse> registrations = registrationRepository
                .findRecentFeeds(postingId, PageRequest.of(0, FEED_LIMIT))
                .stream()
                .map(f -> new PostingDetailResponse.RegistrationFeedResponse(
                        f.getStage(), f.getNickName(), f.getFeedCreatedAt()))
                .toList();

        return new PostingDetailResponse(
                posting.getCompany().getName(),
                posting.getTitle(),
                posting.getCategory().getName(),
                posting.getLocation(),
                posting.getPosition(),
                currentState,
                summary,
                registrations);
    }

    private String resolveCurrentState(Long postingId) {
        List<PostingStageFlatProjection> stages = postingStageRepository
                .findFlatByPostingIds(List.of(postingId));
        stages.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex));

        Map<Long, Long> counts = stageResultCountRepository
                .findTotalsByPostingIds(List.of(postingId)).stream()
                .collect(Collectors.toMap(
                        StageRegistrationCountProjection::getStageId,
                        StageRegistrationCountProjection::getCnt));

        var current = nextStageCalculator.currentStage(stages, counts);
        return current == null ? null : current.getName();
    }
}