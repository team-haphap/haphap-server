package org.sopt.haphap.global.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.domain.DeviceType;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;
import org.sopt.haphap.domain.posting.service.aggregate.StageResultCountRebuilder;
import org.sopt.haphap.domain.registration.domain.ContactMethod;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.search.domain.RelatedSearchKeyword;
import org.sopt.haphap.domain.search.repository.RelatedSearchKeywordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.util.ProfileImageAssigner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int THRESHOLD = 15;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PushTokenRepository pushTokenRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final StageResultCountRebuilder stageResultCountRebuilder;
    private final ProfileImageAssigner profileImageAssigner;
    private final RelatedSearchKeywordRepository relatedSearchKeywordRepository;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "TRUNCATE TABLE registration, posting_stage, stage_result_count, posting, push_token, users, related_search_keyword RESTART IDENTITY CASCADE"
        );
        loadDummyData();
        stageResultCountRebuilder.rebuildAll();
        log.info("=== 기획파트 더미데이터로 초기화 완료 ===");
    }

    private record PostingSeed(
            int sheetId, String company, String title, long categoryId, String location, String position) {
    }

    private record StageSeed(
            int postingSheetId, String name, int orderIndex, LocalDate announceDate, int score) {
    }

    private void loadDummyData() {
        List<PostingSeed> postingSeeds = List.of(
                new PostingSeed(1, "현대자동차", "자율주행개발 - 2026 3월 신입 채용", 5, "남양, 판교", "신입"),
                new PostingSeed(2, "현대자동차", "차량개발 - 2026 3월 신입 채용", 5, "남양", "신입"),
                new PostingSeed(3, "현대자동차", "경영기획 - 2026 3월 신입 채용", 1, "양재", "신입"),
                new PostingSeed(4, "현대자동차", "신사업 전략 기획 - 2026 3월 신입 채용", 1, "양재", "신입"),
                new PostingSeed(5, "현대자동차", "HR - 2026 3월 신입 채용", 3, "양재, 울산", "신입"),
                new PostingSeed(6, "네이버", "Product Design - 2025 신입 공채", 2, "NAVER 1784", "신입"),
                new PostingSeed(7, "카카오", "AI 서비스 개발 - 2026 신입크루 공채", 5, "판교 오피스", "신입"),
                new PostingSeed(8, "카카오", "AI 서비스 운영 - 2026 신입크루 공채", 5, "판교 오피스", "신입"),
                new PostingSeed(9, "아모레퍼시픽", "공간디자인 - 2026 상반기 신입사원 수시채용", 2, "본사", "신입"),
                new PostingSeed(10, "아모레퍼시픽", "생산시스템 엔지니어 - 2026 상반기 신입사원 수시채용", 5, "본사", "신입")
        );

        List<StageSeed> stageSeeds = List.of(
                // posting 1
                new StageSeed(1, "서류", 0, LocalDate.of(2026, 5, 22), 87),
                new StageSeed(1, "인적성검사", 1, LocalDate.of(2026, 6, 5), 23),
                new StageSeed(1, "직무면접", 2, LocalDate.of(2026, 6, 19), 44),
                new StageSeed(1, "임원면접 (최종)", 3, LocalDate.of(2026, 6, 30), 65),
                // posting 2
                new StageSeed(2, "서류", 0, LocalDate.of(2026, 5, 22), 34),
                new StageSeed(2, "인적성검사", 1, LocalDate.of(2026, 6, 5), 80),
                new StageSeed(2, "직무면접", 2, LocalDate.of(2026, 6, 19), 23),
                new StageSeed(2, "임원면접 (최종)", 3, LocalDate.of(2026, 6, 30), 65),
                // posting 3
                new StageSeed(3, "서류", 0, LocalDate.of(2026, 5, 22), 66),
                new StageSeed(3, "인적성검사", 1, LocalDate.of(2026, 6, 5), 23),
                new StageSeed(3, "직무면접", 2, LocalDate.of(2026, 6, 19), 19),
                new StageSeed(3, "임원면접 (최종)", 3, LocalDate.of(2026, 6, 30), 70),
                // posting 4
                new StageSeed(4, "서류", 0, LocalDate.of(2026, 5, 22), 86),
                new StageSeed(4, "인적성검사", 1, LocalDate.of(2026, 6, 5), 58),
                new StageSeed(4, "직무면접", 2, LocalDate.of(2026, 6, 19), 34),
                new StageSeed(4, "임원면접 (최종)", 3, LocalDate.of(2026, 6, 30), 91),
                // posting 5
                new StageSeed(5, "서류", 0, LocalDate.of(2026, 5, 22), 10),
                new StageSeed(5, "인적성검사", 1, LocalDate.of(2026, 6, 5), 45),
                new StageSeed(5, "직무면접", 2, LocalDate.of(2026, 6, 19), 76),
                new StageSeed(5, "임원면접 (최종)", 3, LocalDate.of(2026, 6, 30), 34),
                // posting 6
                new StageSeed(6, "서류", 0, LocalDate.of(2026, 4, 8), 90),
                new StageSeed(6, "1차 면접", 1, LocalDate.of(2026, 4, 30), 45),
                new StageSeed(6, "챌린지", 2, LocalDate.of(2026, 5, 29), 12),
                new StageSeed(6, "2차 면접", 3, LocalDate.of(2026, 6, 27), 45),
                // posting 7
                new StageSeed(7, "서류", 0, LocalDate.of(2026, 11, 10), 67),
                new StageSeed(7, "1차 면접", 1, LocalDate.of(2026, 11, 24), 23),
                new StageSeed(7, "2차 면접", 2, LocalDate.of(2026, 12, 17), 12),
                // posting 8
                new StageSeed(8, "서류", 0, LocalDate.of(2026, 11, 10), 67),
                new StageSeed(8, "1차 면접", 1, LocalDate.of(2026, 11, 24), 34),
                new StageSeed(8, "2차 면접", 2, LocalDate.of(2026, 12, 17), 23),
                // posting 9
                new StageSeed(9, "서류", 0, LocalDate.of(2026, 4, 20), 15),
                new StageSeed(9, "AI역량/영어면접", 1, LocalDate.of(2026, 4, 29), 76),
                new StageSeed(9, "1차 면접", 2, LocalDate.of(2026, 5, 15), 45),
                new StageSeed(9, "2차 면접", 3, LocalDate.of(2026, 6, 24), 63),
                // posting 10
                new StageSeed(10, "서류", 0, LocalDate.of(2026, 4, 20), 85),
                new StageSeed(10, "AI역량/영어면접", 1, LocalDate.of(2026, 4, 29), 95),
                new StageSeed(10, "1차 면접", 2, LocalDate.of(2026, 5, 15), 24),
                new StageSeed(10, "2차 면접", 3, LocalDate.of(2026, 6, 24), 64)
        );

        Map<Integer, Posting> postingBySheetId = new HashMap<>();
        for (PostingSeed s : postingSeeds) {
            Company company = companyRepository.findByName(s.company())
                    .orElseGet(() -> companyRepository.save(Company.create(s.company())));
            Category category = categoryRepository.findById(s.categoryId()).orElseThrow();
            Posting posting = postingRepository.save(
                    Posting.create(s.title(), null, s.location(), s.position(), category, company));
            postingBySheetId.put(s.sheetId(), posting);
        }

        for (StageSeed s : stageSeeds) {
            postingStageRepository.save(PostingStage.create(
                    s.name(), s.orderIndex(), s.announceDate(), s.score(), postingBySheetId.get(s.postingSheetId())));
        }
        List<String> keywords = List.of(
                "카카오", "네이버", "토스", "LG생활건강", "현대자동차", "아모레퍼시픽", "카카오페이", "2026", "삼성전자"
        );
        for (String keyword : keywords) {
            relatedSearchKeywordRepository.save(RelatedSearchKeyword.create(keyword));
        }
    }
}