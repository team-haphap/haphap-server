package org.sopt.haphap.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.alram.service.AlramSettingService;
import org.sopt.haphap.domain.home.service.HomePopularViewTracker;
import org.sopt.haphap.domain.home.service.RecentViewRecorder;
import org.sopt.haphap.domain.posting.code.PostingErrorCode;
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.response.PostingDetailResponse;
import org.sopt.haphap.domain.posting.repository.CompanyImageRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.service.calculator.CurrentStageResolver;
import org.sopt.haphap.domain.registration.service.RegistrationQueryService;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingDetailService {

    private static final int PROFILE_LIMIT = 4;
    private static final int FEED_LIMIT = 30;

    private final PostingRepository postingRepository;
    private final RegistrationQueryService registrationQueryService;
    private final CurrentStageResolver currentStageResolver;
    private final CompanyImageRepository companyImageRepository;
    private final AlramSettingService alramSettingService;
    private final RecentViewRecorder recentViewRecorder;
    private final HomePopularViewTracker homePopularViewTracker;

    public PostingDetailResponse getDetail(Long userId,Long postingId) {
        // 공고 + 회사 + 카테고리
        Posting posting = postingRepository.findWithCompanyAndCategory(postingId)
                .orElseThrow(() -> new CustomException(PostingErrorCode.POSTING_NOT_FOUND));

        // 상세 정상 진입 → 홈 [최근 조회한 공고] 기록 + 홈 [지금 많이 보는 공고] 집계
        if (userId != null) {
            recentViewRecorder.record(userId, posting);
        }
        homePopularViewTracker.record(postingId);

        // currentState 계산 (집계 테이블 재사용)
        String currentState = currentStageResolver.resolveCurrentState(postingId);
        var summary = registrationQueryService.getParticipantSummary(postingId, PROFILE_LIMIT);
        var feeds = registrationQueryService.getRecentFeeds(postingId, FEED_LIMIT);

        List<PostingDetailResponse.ParticipantProfile> profileImages = summary.participants().stream()
                .map(p -> new PostingDetailResponse.ParticipantProfile(p.userId(), p.profileImageUrl()))
                .toList();

        long additional = Math.max(0, summary.registeredCount() - PROFILE_LIMIT);

        String companyImageUrl = companyImageRepository
                .findByCompanyIdAndType(posting.getCompany().getId(), CompanyImageType.DETAIL)
                .map(CompanyImage::getImageUrl)
                .orElse(null);

        boolean alarmEnabled = alramSettingService.isAlarmEnabled(userId, postingId);

        return new PostingDetailResponse(
                posting.getCompany().getName(), posting.getTitle(), posting.getCategory().getName(),
                posting.getLocation(), posting.getPosition(), currentState,
                companyImageUrl,
                new PostingDetailResponse.SummaryResponse(
                        summary.registeredCount(), profileImages, additional),
                feeds.stream().map(f -> new PostingDetailResponse.RegistrationFeedResponse(
                        f.registrationId(),f.stage(), f.nickName(), f.status(), f.feedCreatedAt())).toList(),alarmEnabled);
    }
}