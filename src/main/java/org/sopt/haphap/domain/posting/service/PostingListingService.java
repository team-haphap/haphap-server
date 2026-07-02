package org.sopt.haphap.domain.posting.service;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.*;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.dto.StageRegistrationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostingListingService {

    private final PostingRepository postingRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final NextStageCalculator nextStageCalculator;


    public PopularPostingListResponse getAllPostings(List<String> categoryNames) {
        List<String> filter = (categoryNames == null || categoryNames.isEmpty()) ? null : categoryNames;

        // 1) 카테고리로 전체 공고 (회사·카테고리 fetch join)
        List<Posting> postings = postingRepository.findAllWithCompanyAndCategory(filter);
        if (postings.isEmpty()) {
            return PopularPostingListResponse.from(List.of());
        }
        List<Long> postingIds = postings.stream().map(Posting::getId).toList();

        // 2) 전형 목록 + 전형별 누적 등록수 (nextStage 계산 재료)
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting = postingStageRepository
                .findFlatByPostingIds(postingIds).stream()
                .collect(Collectors.groupingBy(PostingStageFlatProjection::getPostingId));
        stagesByPosting.values()
                .forEach(list -> list.sort(Comparator.comparingInt(PostingStageFlatProjection::getOrderIndex)));

        Map<Long, Map<Long, Long>> countsByPosting = registrationRepository
                .countByPostingAndStage(postingIds).stream()
                .collect(Collectors.groupingBy(
                        StageRegistrationCountProjection::getPostingId,
                        Collectors.toMap(
                                StageRegistrationCountProjection::getStageId,
                                StageRegistrationCountProjection::getCnt)));

        // 3) 각 공고 응답 + 발표일 계산 → 발표일 가까운 순 정렬
        Collator korean = Collator.getInstance(Locale.KOREAN);
        List<PopularPostingResponse> result = postings.stream()
                .map(p -> toScored(p,
                        stagesByPosting.getOrDefault(p.getId(), List.of()),
                        countsByPosting.getOrDefault(p.getId(), Map.of())))
                .sorted(announceDateComparator(korean))
                .map(Scored::response)
                .toList();

        return PopularPostingListResponse.from(result);
    }

    private Scored toScored(Posting posting,
                            List<PostingStageFlatProjection> stages,
                            Map<Long, Long> counts) {
        PostingStageFlatProjection nextStage = nextStageCalculator.calculate(stages, counts);
        Integer days = nextStageCalculator.daysUntil(nextStage);
        LocalDate announceDate = (nextStage == null) ? null : nextStage.getExpectedAnnouncementDate();

        PopularPostingResponse response = new PopularPostingResponse(
                posting.getId(), posting.getTitle(),
                posting.getCompany().getName(), posting.getCategory().getName(),
                posting.getCompany().getDescription(),
                nextStage == null ? null : nextStage.getName(),
                days, posting.getCompany().getImageUrl());
        return new Scored(response, posting.getTitle(), announceDate);
    }

    // 발표일 가까운 순(과거·null 뒤), 같으면 공고명 가나다
    private Comparator<Scored> announceDateComparator(Collator korean) {
        LocalDate today = LocalDate.now();
        return Comparator
                .comparing((Scored s) -> rank(s.announceDate(), today))
                .thenComparing(Scored::announceDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Scored::title, korean);
    }

    private int rank(LocalDate date, LocalDate today) {
        if (date == null) return 2;
        return date.isBefore(today) ? 1 : 0;
    }

    private record Scored(PopularPostingResponse response, String title, LocalDate announceDate) {}
}