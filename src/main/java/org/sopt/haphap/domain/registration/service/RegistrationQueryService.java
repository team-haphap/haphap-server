package org.sopt.haphap.domain.registration.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.dto.ParticipantSummary;
import org.sopt.haphap.domain.posting.dto.RegistrationFeed;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.projection.RecentParticipantProjection;
import org.sopt.haphap.domain.registration.projection.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.projection.StageResultAggProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationQueryService {

    private final RegistrationRepository registrationRepository;

    // 참여 요약: 유저 수 + 최근 참여자 프로필
    public ParticipantSummary getParticipantSummary(Long postingId, int profileLimit) {
        long count = registrationRepository.countDistinctUsersByPostingId(postingId);
        List<ParticipantSummary.Participant> participants = registrationRepository
                .findRecentParticipants(postingId, PageRequest.of(0, profileLimit))
                .stream()
                .map(p -> new ParticipantSummary.Participant(p.getUserId(), p.getProfileImageUrl()))
                .toList();
        return new ParticipantSummary(count, participants);
    }

    // 실시간 제보
    public List<RegistrationFeed> getRecentFeeds(Long postingId, int limit) {
        return registrationRepository
                .findRecentFeeds(postingId, PageRequest.of(0, limit))
                .stream()
                .map(f -> new RegistrationFeed(f.getRegistrationId(),f.getStage(), f.getNickName(), f.getStatus(),f.getFeedCreatedAt()))
                .toList();
    }

    public List<Long> findRecentlyActivePostingIds(List<RegistrationResult> results, LocalDateTime since, List<String> categoryFilter) {
        return registrationRepository.findRecentlyActivePostingIds(results, since, categoryFilter);
    }

    // 48h (공고,전형)별 등록수 — 인기 공고 정렬용
    public List<StageRegistrationCountProjection> countRecentActiveByPostingAndStage(
            List<RegistrationResult> results, LocalDateTime since, List<Long> postingIds) {
        return registrationRepository.countRecentActiveByPostingAndStage(results, since, postingIds);
    }

    // (공고,전형,결과)별 전체 집계 — StageResultCount 재구성/정합성 보정용
    public List<StageResultAggProjection> aggregateAllForRebuild() {
        return registrationRepository.aggregateAllForRebuild();
    }

    // 오늘 등록/변경된 결과 수
    public Long countTodayEvents(LocalDateTime startOfDay, LocalDateTime startOfTomorrow) {
        return registrationRepository.countTodayEvents(startOfDay, startOfTomorrow);
    }
}