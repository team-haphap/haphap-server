package org.sopt.haphap.domain.mypage.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.mypage.dto.response.MyPageSummaryResponse;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.registration.projection.PassCertificationCountProjection;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageSummaryService {

    private final UserService userService;
    private final RegistrationRepository registrationRepository;
    private final PostingStageRepository postingStageRepository;

    // 마이페이지 상단 요약: 회원 정보 + 합격 인증 건수 + 이번 주 발표 예상 여부. 쿼리 3번(유저, 집계 1행, exists).
    public MyPageSummaryResponse getSummary(Long userId) {
        User user = userService.findById(userId);
        PassCertificationCountProjection counts = registrationRepository.countPassCertifications(userId);
        return MyPageSummaryResponse.of(user, counts, hasAnnouncementExpectedThisWeek(LocalDate.now()));
    }

    // 이번 주 = 오늘이 속한 주의 월요일 ~ 일요일 (이미 지난 요일 포함)
    private boolean hasAnnouncementExpectedThisWeek(LocalDate today) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return postingStageRepository.existsByExpectedAnnouncementDateBetween(monday, sunday);
    }
}
