package org.sopt.haphap.global.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.sopt.haphap.global.util.ProfileImageAssigner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
/*
@Slf4j
@Component
//@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int THRESHOLD = 15;

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PushTokenRepository pushTokenRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final StageResultCountRebuilder stageResultCountRebuilder;
    private final ProfileImageAssigner profileImageAssigner;

    @Override
    public void run(String... args) {

        if (postingRepository.count() > 0) {
            log.info("=== 초기 데이터가 이미 존재하여 DataInitializer를 건너뜁니다 ===");
            return;
        }

        // ── 유저 40명 (프로필 이미지는 ProfileImageAssigner가 DB profile_image에서 랜덤 배정) ──
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            users.add(userRepository.save(User.builder()
                    .anonymousName("익명" + i)
                    .name("유저" + i)
                    .email("user" + i + "@test.com")
                    .provider(Provider.KAKAO)
                    .providerId("user-" + i)
                    .profileImageUrl(profileImageAssigner.assign())
                    .build()));
        }


        Category pm = categoryRepository.findByName("기획")
                .orElseThrow();

        Category design = categoryRepository.findByName("디자인")
                .orElseThrow();

        Category hr = categoryRepository.findByName("인사")
                .orElseThrow();

        Category sales = categoryRepository.findByName("영업")
                .orElseThrow();

        Category dev = categoryRepository.findByName("개발/데이터")
                .orElseThrow();

        Category finance = categoryRepository.findByName("금융/보험")
                .orElseThrow();

        Company toss = companyRepository.findByName("토스")
                .orElseThrow();

        Company kakao = companyRepository.findByName("카카오")
                .orElseThrow();

        Company naver = companyRepository.findByName("네이버")
                .orElseThrow();

        Company amore = companyRepository.findByName("아모레퍼시픽")
                .orElseThrow();

        Company hyundai = companyRepository.findByName("현대자동차")
                .orElseThrow();

        Company lg = companyRepository.findByName("LG")
                .orElseThrow();
        // ============================================================
        // 기존 시나리오: 인기순(/postings) · 발표일순(/postings/all)
        // ============================================================

        Posting a = postingRepository.save(
                Posting.create("A 백엔드 신입 공채", LocalDate.now().plusDays(60),
                        "판교 본사", "백엔드 개발자", dev, toss));
        List<PostingStage> as = addStages(a,
                stage("서류", 1, LocalDate.now().minusDays(10), 10),
                stage("코딩 테스트", 2, LocalDate.now().minusDays(2), 10),
                stage("1차 면접", 3, LocalDate.now().plusDays(20), 10),
                stage("최종", 4, LocalDate.now().plusDays(40), 10));
        register(users, a, as.get(0), 25, RegistrationResult.PASS);
        register(users, a, as.get(1), 20, RegistrationResult.PASS);

        Posting b = postingRepository.save(
                Posting.create("B 프론트 신입 공채", LocalDate.now().plusDays(60),
                        "역삼 지사", "프론트엔드 개발자", dev, kakao));
        List<PostingStage> bs = addStages(b,
                stage("서류", 1, LocalDate.now().minusDays(1), 10),
                stage("코딩 테스트", 2, LocalDate.now().plusDays(1), 10),
                stage("1차 면접", 3, LocalDate.now().plusDays(10), 10),
                stage("최종", 4, LocalDate.now().plusDays(20), 10));
        register(users, b, bs.get(0), 18, RegistrationResult.PASS);

        Posting c = postingRepository.save(
                Posting.create("C 기획 신입 공채", LocalDate.now().plusDays(60),
                        "양재 본사", "책임매니저", pm, naver));
        List<PostingStage> cs = addStages(c,
                stage("서류", 1, LocalDate.now().minusDays(3), 10),
                stage("코딩 테스트", 2, LocalDate.now().minusDays(1), 10),
                stage("1차 면접", 3, LocalDate.now().plusDays(5), 10),
                stage("최종", 4, LocalDate.now().plusDays(15), 10));
        register(users, c, cs.get(0), 20, RegistrationResult.PASS);
        register(users, c, cs.get(1), 16, RegistrationResult.PASS);

        Posting d = postingRepository.save(
                Posting.create("D 마케팅 신입 공채", LocalDate.now().plusDays(60),
                        "성수 오피스", "프로덕트 디자이너", design, toss));
        List<PostingStage> ds = addStages(d,
                stage("서류", 1, LocalDate.now().plusDays(3), 10),
                stage("최종", 2, LocalDate.now().plusDays(12), 10));
        register(users, d, ds.get(0), 10, RegistrationResult.PENDING);

        Posting e = postingRepository.save(
                Posting.create("E 백엔드 신입 공채", LocalDate.now().plusDays(60),
                        "분당 센터", "백엔드 개발자", dev, naver));
        List<PostingStage> es = addStages(e,
                stage("서류", 1, LocalDate.now().plusDays(7), 10),
                stage("1차 면접", 2, LocalDate.now().plusDays(14), 10));
        register(users, e, es.get(0), 5, RegistrationResult.PASS);

        int[] counts = {35, 32, 28, 15, 12, 8};
        String[] names = {"F", "G", "H", "I", "J", "K"};
        for (int i = 0; i < names.length; i++) {
            Posting p = postingRepository.save(
                    Posting.create(names[i] + " 개발 공채", LocalDate.now().plusDays(60),
                            "본사", "개발자", dev, toss));
            List<PostingStage> ps = addStages(p,
                    stage("서류", 1, LocalDate.now().plusDays(10 + i), 10));
            register(users, p, ps.get(0), counts[i], RegistrationResult.PASS);
        }

        // ============================================================
        // 오늘 발표 예상(/announcements): 발표일=오늘, score 상위 3개
        // ============================================================
        Posting t1 = postingRepository.save(
                Posting.create("오늘발표 하이스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, kakao));
        addStages(t1, stage("1차 면접", 1, LocalDate.now(), 95));

        Posting t2 = postingRepository.save(
                Posting.create("오늘발표 미드스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, toss));
        addStages(t2, stage("서류", 1, LocalDate.now(), 80));

        Posting t3 = postingRepository.save(
                Posting.create("오늘발표 로우스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "기획자", pm, naver));
        addStages(t3, stage("코딩 테스트", 1, LocalDate.now(), 60));

        Posting t4 = postingRepository.save(
                Posting.create("오늘발표 컷오프 공채", LocalDate.now().plusDays(60),
                        "본사", "마케팅", design, kakao));
        addStages(t4, stage("최종", 1, LocalDate.now(), 40));

        Posting t5 = postingRepository.save(
                Posting.create("내일발표 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, naver));
        addStages(t5, stage("서류", 1, LocalDate.now().plusDays(1), 99));

        // ============================================================
        // 공고 상세(/postings/{id}/detail) 검증 전용
        // ============================================================
        Posting detail = postingRepository.save(
                Posting.create("카카오 에너지 태양광 사업 정책기획", LocalDate.now().plusDays(60),
                        "양재 본사", "책임매니저", pm, kakao));
        List<PostingStage> ds2 = addStages(detail,
                stage("서류", 1, LocalDate.now().minusDays(2), 50),
                stage("코딩 테스트", 2, LocalDate.now().plusDays(1), 50),
                stage("1차 면접", 3, LocalDate.now().plusDays(5), 50),
                stage("최종", 4, LocalDate.now().plusDays(12), 50));
        registerResults(users, detail, ds2.get(0), 12, RegistrationResult.PASS, 0);
        registerResults(users, detail, ds2.get(0), 4, RegistrationResult.FAIL, 12);
        registerResults(users, detail, ds2.get(0), 4, RegistrationResult.PENDING, 16);
        registerResults(users, detail, ds2.get(1), 16, RegistrationResult.PASS, 0);
        registerResults(users, detail, ds2.get(2), 3, RegistrationResult.PENDING, 0);

        pushTokenRepository.save(
                PushToken.create(users.get(0), "device-001", "token-001", DeviceType.ANDROID));

        stageResultCountRebuilder.rebuildAll();

        log.info("=== 테스트 데이터 준비 완료 (임계값 {}) ===", THRESHOLD);
        log.info("[/postings 인기순, 개발] F(35)>G(32)>H(28)>A(20)>B(18)>I(15)>J(12)>K(8) 상위8, E(5) 잘림");
        log.info("[/postings/all 개발] 발표일순: B(+1)→E(+7)→F(+10)→...→A(1차,+20)");
        log.info("[/announcements] T1(95)>T2(80)>T3(60) 3개, T4(40) 잘림, T5(내일) 제외");
        log.info("[/postings/{}/detail] 상세 검증: 현재진행=코테, registeredCount≈20, 제보 다수", detail.getId());
        log.info("[C 인기순 제외 SQL] UPDATE registration SET updated_at = now() - interval '3 days' " +
                "WHERE posting_id = {} AND stage_id = {};", c.getId(), cs.get(1).getId());
    }

    // ── 헬퍼 ──

    private record StageSpec(String name, int orderIndex, LocalDate announceDate, int score) {}

    private StageSpec stage(String name, int orderIndex, LocalDate announceDate, int score) {
        return new StageSpec(name, orderIndex, announceDate, score);
    }

    private List<PostingStage> addStages(Posting posting, StageSpec... specs) {
        List<PostingStage> saved = new ArrayList<>();
        for (StageSpec s : specs) {
            saved.add(postingStageRepository.save(
                    PostingStage.create(s.name(), s.orderIndex(), s.announceDate(), s.score(), posting)));
        }
        return saved;
    }

    private void register(List<User> users, Posting posting, PostingStage stage,
                          int count, RegistrationResult result) {
        registerResults(users, posting, stage, count, result, 0);
    }

    private void registerResults(List<User> users, Posting posting, PostingStage stage,
                                 int count, RegistrationResult result, int offset) {
        if (offset + count > users.size()) {
            throw new IllegalArgumentException(
                    "offset+count(" + (offset + count) + ") > 유저수(" + users.size() + ").");
        }
        for (int i = 0; i < count; i++) {
            registrationRepository.save(
                    Registration.create(users.get(offset + i), posting, stage, result,
                            List.of(ContactMethod.EMAIL), LocalDateTime.now(), false));
        }
    }
}

 */


/**
 * 스프레드시트(공고 11건 + 전형 41건) 기반 더미데이터 초기화.
 *
 * 등록(Registration) 규칙
 *  - posting 1 : 임원면접(최종) 이전 전형(서류/인적성/직무면접) 전부 5건 이상
 *  - posting 2 : 직무면접까지 전부 5건 이상
 *  - 그 외 공고 : expectedAnnouncementDate 가 TODAY(2026-07-16) 이전인 전형은 전부 5건 이상
 *  - posting 11(삼성전자) : 시트에 발표일이 없어 전형별 등록 없음
 */
@Slf4j
@Component
//@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int THRESHOLD = 15;

    /** 시트 기준 "오늘". 등록 여부 판단 기준일 */
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 16);

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PushTokenRepository pushTokenRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final StageResultCountRebuilder stageResultCountRebuilder;
    private final ProfileImageAssigner profileImageAssigner;

    @Override
    public void run(String... args) {

        if (postingRepository.count() > 0) {
            log.info("=== 초기 데이터가 이미 존재하여 DataInitializer를 건너뜁니다 ===");
            return;
        }

        // ── 유저 40명 ──
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            users.add(userRepository.save(User.builder()
                    .anonymousName("익명" + i)
                    .name("유저" + i)
                    .email("user" + i + "@test.com")
                    .provider(Provider.KAKAO)
                    .providerId("user-" + i)
                    .profileImageUrl(profileImageAssigner.assign())
                    .build()));
        }

        // ── 카테고리 ──
        Category pm = category("기획");
        Category design = category("디자인");
        Category hr = category("인사");
        Category sales = category("영업");      // 시트에 해당 공고 없음
        Category dev = category("개발/데이터");
        Category finance = category("금융/보험"); // 시트에 해당 공고 없음

        // ── 회사 ──
        Company toss = company("토스");         // 시트에 해당 공고 없음
        Company kakao = company("카카오");
        Company naver = company("네이버");
        Company amore = company("아모레퍼시픽");
        Company hyundai = company("현대자동차");
        Company lg = company("LG");             // 시트에 해당 공고 없음
        //Company samsung = company("LG");  // ★ company 테이블에 미리 넣어두어야 함

        // =====================================================================
        // 1. 현대자동차 - 자율주행개발
        //    임원면접(최종, 7/16 = 오늘) 이전 전형 전부 등록 5건 이상
        // =====================================================================
        Posting p1 = posting("자율주행개발 - 2026 3월 신입 채용", "남양, 판교", dev, hyundai);
        List<PostingStage> s1 = addStages(p1,
                stage("서류", 1, LocalDate.of(2026, 5, 22), 87),
                stage("인적성검사", 2, LocalDate.of(2026, 6, 5), 23),
                stage("직무면접", 3, LocalDate.of(2026, 6, 19), 44),
                stage("임원면접 (최종)", 4, LocalDate.of(2026, 7, 16), 65));
        registerResults(users, p1, s1.get(0), 20, RegistrationResult.PASS, 0);
        registerResults(users, p1, s1.get(0), 6, RegistrationResult.FAIL, 20);
        registerResults(users, p1, s1.get(0), 4, RegistrationResult.PENDING, 26);
        registerResults(users, p1, s1.get(1), 12, RegistrationResult.PASS, 0);
        registerResults(users, p1, s1.get(1), 5, RegistrationResult.FAIL, 12);
        register(users, p1, s1.get(2), 8, RegistrationResult.PASS);
        // s1.get(3) 임원면접(최종) → 등록 없음

        // =====================================================================
        // 2. 현대자동차 - 차량개발
        //    직무면접까지 등록 5건 이상 / 임원면접(7/30, 미래)은 등록 없음
        // =====================================================================
        Posting p2 = posting("차량개발 - 2026 3월 신입 채용", "남양", dev, hyundai);
        List<PostingStage> s2 = addStages(p2,
                stage("서류", 1, LocalDate.of(2026, 5, 22), 34),
                stage("인적성검사", 2, LocalDate.of(2026, 6, 5), 80),
                stage("직무면접", 3, LocalDate.of(2026, 6, 16), 23),  // 시트 0.234
                stage("임원면접 (최종)", 4, LocalDate.of(2026, 7, 30), 65));
        registerResults(users, p2, s2.get(0), 18, RegistrationResult.PASS, 0);
        registerResults(users, p2, s2.get(0), 5, RegistrationResult.FAIL, 18);
        register(users, p2, s2.get(1), 11, RegistrationResult.PASS);
        register(users, p2, s2.get(2), 6, RegistrationResult.PASS);
        // s2.get(3) 임원면접(최종) → 등록 없음

        // =====================================================================
        // 3. 현대자동차 - 경영기획 (전 전형 발표일이 오늘 이전 → 전부 5건 이상)
        // =====================================================================
        Posting p3 = posting("경영기획 - 2026 3월 신입 채용", "양재", pm, hyundai);
        List<PostingStage> s3 = addStages(p3,
                stage("서류", 1, LocalDate.of(2026, 5, 22), 66),
                stage("인적성검사", 2, LocalDate.of(2026, 6, 5), 23),
                stage("직무면접", 3, LocalDate.of(2026, 6, 19), 19),
                stage("임원면접 (최종)", 4, LocalDate.of(2026, 6, 30), 70));
        register(users, p3, s3.get(0), 15, RegistrationResult.PASS);
        register(users, p3, s3.get(1), 9, RegistrationResult.PASS);
        register(users, p3, s3.get(2), 6, RegistrationResult.PASS);
        register(users, p3, s3.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 4. 현대자동차 - 신사업 전략 기획
        // =====================================================================
        Posting p4 = posting("신사업 전략 기획 - 2026 3월 신입 채용", "양재", pm, hyundai);
        List<PostingStage> s4 = addStages(p4,
                stage("서류", 1, LocalDate.of(2026, 5, 22), 86),
                stage("인적성검사", 2, LocalDate.of(2026, 6, 5), 58),
                stage("직무면접", 3, LocalDate.of(2026, 6, 19), 34),
                stage("임원면접 (최종)", 4, LocalDate.of(2026, 6, 30), 91));
        register(users, p4, s4.get(0), 12, RegistrationResult.PASS);
        register(users, p4, s4.get(1), 8, RegistrationResult.PASS);
        register(users, p4, s4.get(2), 6, RegistrationResult.PASS);
        register(users, p4, s4.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 5. 현대자동차 - HR
        // =====================================================================
        Posting p5 = posting("HR - 2026 3월 신입 채용", "양재, 울산", hr, hyundai);
        List<PostingStage> s5 = addStages(p5,
                stage("서류", 1, LocalDate.of(2026, 5, 22), 10),
                stage("인적성검사", 2, LocalDate.of(2026, 6, 5), 45),
                stage("직무면접", 3, LocalDate.of(2026, 6, 19), 76),
                stage("임원면접 (최종)", 4, LocalDate.of(2026, 6, 30), 34));
        register(users, p5, s5.get(0), 10, RegistrationResult.PASS);
        register(users, p5, s5.get(1), 7, RegistrationResult.PASS);
        register(users, p5, s5.get(2), 5, RegistrationResult.PASS);
        register(users, p5, s5.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 6. 네이버 - Product Design (공고명이 "2025 신입 공채" → 2025년으로 가정)
        // =====================================================================
        Posting p6 = posting("Product Design - 2025 신입 공채", "NAVER 1784", design, naver);
        List<PostingStage> s6 = addStages(p6,
                stage("서류", 1, LocalDate.of(2025, 4, 8), 90),
                stage("1차 면접", 2, LocalDate.of(2025, 4, 30), 45),
                stage("챌린지", 3, LocalDate.of(2025, 5, 29), 12),
                stage("2차 면접", 4, LocalDate.of(2025, 6, 27), 45));
        registerResults(users, p6, s6.get(0), 22, RegistrationResult.PASS, 0);
        registerResults(users, p6, s6.get(0), 6, RegistrationResult.FAIL, 22);
        register(users, p6, s6.get(1), 14, RegistrationResult.PASS);
        register(users, p6, s6.get(2), 9, RegistrationResult.PASS);
        register(users, p6, s6.get(3), 6, RegistrationResult.PASS);

        // =====================================================================
        // 7. 카카오 - AI 서비스 개발 (11~12월 → 2025년 하반기로 가정)
        // =====================================================================
        Posting p7 = posting("AI 서비스 개발 - 2026 신입크루 공채", "판교 오피스", dev, kakao);
        List<PostingStage> s7 = addStages(p7,
                stage("서류", 1, LocalDate.of(2025, 11, 10), 67),
                stage("1차 면접", 2, LocalDate.of(2025, 11, 24), 23),
                stage("2차 면접", 3, LocalDate.of(2025, 12, 17), 12));
        registerResults(users, p7, s7.get(0), 32, RegistrationResult.PASS, 0);
        registerResults(users, p7, s7.get(0), 8, RegistrationResult.FAIL, 32);
        register(users, p7, s7.get(1), 19, RegistrationResult.PASS);
        register(users, p7, s7.get(2), 10, RegistrationResult.PASS);

        // =====================================================================
        // 8. 카카오 - AI 서비스 운영
        // =====================================================================
        Posting p8 = posting("AI 서비스 운영 - 2026 신입크루 공채", "판교 오피스", dev, kakao);
        List<PostingStage> s8 = addStages(p8,
                stage("서류", 1, LocalDate.of(2025, 11, 10), 67),
                stage("1차 면접", 2, LocalDate.of(2025, 11, 24), 34),
                stage("2차 면접", 3, LocalDate.of(2025, 12, 17), 23));
        register(users, p8, s8.get(0), 25, RegistrationResult.PASS);
        register(users, p8, s8.get(1), 13, RegistrationResult.PASS);
        register(users, p8, s8.get(2), 7, RegistrationResult.PASS);

        // =====================================================================
        // 9. 아모레퍼시픽 - 공간디자인
        // =====================================================================
        Posting p9 = posting("공간디자인 - 2026 상반기 신입사원 수시채용", "본사", design, amore);
        List<PostingStage> s9 = addStages(p9,
                stage("서류", 1, LocalDate.of(2026, 4, 20), 15),
                stage("AI역량/영어면접", 2, LocalDate.of(2026, 4, 29), 76),
                stage("1차 면접", 3, LocalDate.of(2026, 5, 15), 45),
                stage("2차 면접", 4, LocalDate.of(2026, 6, 24), 63));
        registerResults(users, p9, s9.get(0), 16, RegistrationResult.PASS, 0);
        registerResults(users, p9, s9.get(0), 5, RegistrationResult.PENDING, 16);
        register(users, p9, s9.get(1), 11, RegistrationResult.PASS);
        register(users, p9, s9.get(2), 7, RegistrationResult.PASS);
        register(users, p9, s9.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 10. 아모레퍼시픽 - 생산시스템 엔지니어
        // =====================================================================
        Posting p10 = posting("생산시스템 엔지니어 - 2026 상반기 신입사원 수시채용", "본사", dev, amore);
        List<PostingStage> s10 = addStages(p10,
                stage("서류", 1, LocalDate.of(2026, 4, 20), 85),
                stage("AI역량/영어면접", 2, LocalDate.of(2026, 4, 29), 95),
                stage("1차 면접", 3, LocalDate.of(2026, 5, 15), 24),
                stage("2차 면접", 4, LocalDate.of(2026, 6, 24), 64));
        register(users, p10, s10.get(0), 14, RegistrationResult.PASS);
        register(users, p10, s10.get(1), 9, RegistrationResult.PASS);
        register(users, p10, s10.get(2), 6, RegistrationResult.PASS);
        register(users, p10, s10.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 11. LG - SW개발 (시트에 발표일/점수 없음 → 발표일 null, score 0, 등록 없음)
        // =====================================================================
        Posting p11 = posting("SW개발 - 2026년 상반기 3급 신입사원 채용 공고", "화성", dev, lg);
        List<PostingStage> s11 = addStages(p11,
                stage("지원서 접수", 1, LocalDate.of(2026, 3, 30), 36),
                stage("직무 적합성 평가", 2, LocalDate.of(2026, 4, 6), 59),
                stage("직무 적성 검사", 3, LocalDate.of(2026, 4, 25), 25),
                stage("면접", 4, LocalDate.of(2026, 5, 10), 88));
        registerResults(users, p11, s11.get(0), 30, RegistrationResult.PASS, 0);
        registerResults(users, p11, s11.get(0), 7, RegistrationResult.FAIL, 30);
        register(users, p11, s11.get(1), 18, RegistrationResult.PASS);
        register(users, p11, s11.get(2), 10, RegistrationResult.PASS);
        register(users, p11, s11.get(3), 6, RegistrationResult.PASS);

        // =====================================================================
        // 12. LG생활건강 - 2025년 하반기 신입사원 채용 (공고명 "2025년" → 2025)
        // =====================================================================
        Posting p12 = posting("2025년 하반기 신입사원 채용 (마케팅 / 영업)", "종로구", sales, lg);
        List<PostingStage> s12 = addStages(p12,
                stage("서류 전형", 1, LocalDate.of(2025, 5, 27), 22),
                stage("인적성 검사", 2, LocalDate.of(2025, 6, 11), 46),
                stage("1차 면접", 3, LocalDate.of(2025, 6, 20), 39),
                stage("2차 면접", 4, LocalDate.of(2025, 7, 1), 65),
                stage("인턴십 전형", 5, LocalDate.of(2025, 7, 14), 43));
        registerResults(users, p12, s12.get(0), 24, RegistrationResult.PASS, 0);
        registerResults(users, p12, s12.get(0), 6, RegistrationResult.FAIL, 24);
        register(users, p12, s12.get(1), 15, RegistrationResult.PASS);
        register(users, p12, s12.get(2), 10, RegistrationResult.PASS);
        register(users, p12, s12.get(3), 7, RegistrationResult.PASS);
        register(users, p12, s12.get(4), 5, RegistrationResult.PASS);

        // =====================================================================
        // 13. 카카오 - LLM Research Engineer (9~10월, 2026 가정 → 전부 미래 / 등록 없음)
        // =====================================================================
        Posting p13 = posting("LLM Research Engineer (Pre-training) 신입 채용", "판교 오피스", dev, kakao);
        addStages(p13,
                stage("서류 전형", 1, LocalDate.of(2026, 9, 4), 34),
                stage("코딩테스트", 2, LocalDate.of(2026, 9, 18), 58),
                stage("1차 면접", 3, LocalDate.of(2026, 10, 4), 78),
                stage("2차 면접", 4, LocalDate.of(2026, 10, 13), 63));

        // =====================================================================
        // 14. 카카오 - 카카오쇼핑 판매자 성장지원 담당자 (3~4월, 2026 가정)
        // =====================================================================
        Posting p14 = posting("카카오쇼핑(톡딜/선물하기) 판매자 성장지원 담당자 신입", "판교 오피스", pm, kakao);
        List<PostingStage> s14 = addStages(p14,
                stage("서류 전형", 1, LocalDate.of(2026, 3, 5), 55),   // 시트 오타 "서류 젼형" 수정
                stage("1차 면접", 2, LocalDate.of(2026, 3, 22), 26),
                stage("2차 면접", 3, LocalDate.of(2026, 4, 3), 49));
        register(users, p14, s14.get(0), 20, RegistrationResult.PASS);
        register(users, p14, s14.get(1), 12, RegistrationResult.PASS);
        register(users, p14, s14.get(2), 6, RegistrationResult.PASS);

        // =====================================================================
        // 15. 네이버 - 2025 팀네이버 신입 공채 (공고명 "2025" → 2025)
        // =====================================================================
        Posting p15 = posting("2025 팀네이버 신입 공채", "분당", pm, naver);
        List<PostingStage> s15 = addStages(p15,
                stage("서류 전형", 1, LocalDate.of(2025, 7, 9), 65),
                stage("프로덕트 디벨롭 인터뷰", 2, LocalDate.of(2025, 7, 18), 77),
                stage("챌린지 전형 & 종합 역량 인터뷰", 3, LocalDate.of(2025, 8, 1), 94));
        registerResults(users, p15, s15.get(0), 30, RegistrationResult.PASS, 0);
        registerResults(users, p15, s15.get(0), 8, RegistrationResult.FAIL, 30);
        register(users, p15, s15.get(1), 20, RegistrationResult.PASS);
        register(users, p15, s15.get(2), 11, RegistrationResult.PASS);

        // =====================================================================
        // 16. 네이버 - 백엔드 서버 개발 (1~2월, 2026 가정)
        // =====================================================================
        Posting p16 = posting("백엔드 서버 개발 신입 채용", "NAVER 1784", dev, naver);
        List<PostingStage> s16 = addStages(p16,
                stage("지원서 리뷰", 1, LocalDate.of(2026, 1, 14), 38),
                stage("프리 인터뷰", 2, LocalDate.of(2026, 1, 29), 56),
                stage("실무 인터뷰", 3, LocalDate.of(2026, 2, 8), 23),
                stage("Culture-Fit 인터뷰", 4, LocalDate.of(2026, 2, 17), 75));
        registerResults(users, p16, s16.get(0), 28, RegistrationResult.PASS, 0);
        registerResults(users, p16, s16.get(0), 6, RegistrationResult.FAIL, 28);
        register(users, p16, s16.get(1), 17, RegistrationResult.PASS);
        register(users, p16, s16.get(2), 9, RegistrationResult.PASS);
        register(users, p16, s16.get(3), 5, RegistrationResult.PASS);

        // =====================================================================
        // 17. 네이버 - IT 보안 기술 담당 (5~6월, 2026 가정)
        // =====================================================================
        Posting p17 = posting("IT 보안 기술 담당 신입 채용", "NAVER 1784", dev, naver);
        List<PostingStage> s17 = addStages(p17,
                stage("서류 전형", 1, LocalDate.of(2026, 5, 11), 17),
                stage("1차 인터뷰 & 기업문화 적합도 검사", 2, LocalDate.of(2026, 5, 30), 62),
                stage("2차 인터뷰", 3, LocalDate.of(2026, 6, 9), 59));
        register(users, p17, s17.get(0), 16, RegistrationResult.PASS);
        register(users, p17, s17.get(1), 10, RegistrationResult.PASS);
        register(users, p17, s17.get(2), 6, RegistrationResult.PASS);

        // =====================================================================
        // 18. 네이버 - 쇼핑 신사업 개발 담당 (3~4월, 2026 가정)
        // =====================================================================
        Posting p18 = posting("쇼핑 신사업 개발 담당 신입 채용", "NAVER 1784", sales, naver);
        List<PostingStage> s18 = addStages(p18,
                stage("서류전형&기업문화 적합도 검사", 1, LocalDate.of(2026, 3, 22), 28),
                stage("직무 인터뷰", 2, LocalDate.of(2026, 4, 7), 57),
                stage("종합 인터뷰", 3, LocalDate.of(2026, 4, 24), 78));
        register(users, p18, s18.get(0), 13, RegistrationResult.PASS);
        register(users, p18, s18.get(1), 8, RegistrationResult.PASS);
        register(users, p18, s18.get(2), 5, RegistrationResult.PASS);

        // =====================================================================
        // 19. 네이버 - Embodied AI Research Engineer (11~12월, 2026 가정 → 전부 미래)
        // =====================================================================
        Posting p19 = posting("Embodied AI Research Engineer 신입 채용", "NAVER 1784", dev, naver);
        addStages(p19,
                stage("서류 전형", 1, LocalDate.of(2026, 11, 13), 53),
                stage("전화 면접", 2, LocalDate.of(2026, 11, 21), 31),
                stage("1차 면접", 3, LocalDate.of(2026, 11, 30), 67),
                stage("2차 면접", 4, LocalDate.of(2026, 12, 8), 48));

        // =====================================================================
        // 20. 네이버 - 헬스케어 산업 리서치 (8월, 2026 가정 → 전부 미래)
        // =====================================================================
        Posting p20 = posting("헬스케어 산업 리서치 신입 채용", "NAVER 1784", sales, naver);
        addStages(p20,
                stage("서류 전형", 1, LocalDate.of(2026, 8, 7), 22),
                stage("직무 인터뷰", 2, LocalDate.of(2026, 8, 20), 76));

        // =====================================================================
        // 21. 토스 - Security Research Specialist (4~5월, 2026 가정)
        // =====================================================================
        Posting p21 = posting("Security Research Specialist 신입 채용", "강남", dev, toss);
        List<PostingStage> s21 = addStages(p21,
                stage("서류 전형", 1, LocalDate.of(2026, 4, 23), 69),
                stage("직무 인터뷰", 2, LocalDate.of(2026, 5, 1), 43),
                stage("문화적합성 인터뷰", 3, LocalDate.of(2026, 5, 9), 56));
        register(users, p21, s21.get(0), 21, RegistrationResult.PASS);
        register(users, p21, s21.get(1), 12, RegistrationResult.PASS);
        register(users, p21, s21.get(2), 7, RegistrationResult.PASS);

        // =====================================================================
        // 22. 토스 - 여신 상품 Manager (6월, 2026 가정)
        // =====================================================================
        Posting p22 = posting("여신 상품 Manager 신입 채용", "강남", sales, toss);
        List<PostingStage> s22 = addStages(p22,
                stage("서류 전형", 1, LocalDate.of(2026, 6, 5), 45),
                stage("직무 인터뷰", 2, LocalDate.of(2026, 6, 13), 63),
                stage("문화적합성 인터뷰", 3, LocalDate.of(2026, 6, 20), 24),
                stage("레퍼런스 체크", 4, LocalDate.of(2026, 6, 29), 87));
        registerResults(users, p22, s22.get(0), 18, RegistrationResult.PASS, 0);
        registerResults(users, p22, s22.get(0), 5, RegistrationResult.PENDING, 18);
        register(users, p22, s22.get(1), 11, RegistrationResult.PASS);
        register(users, p22, s22.get(2), 7, RegistrationResult.PASS);
        register(users, p22, s22.get(3), 5, RegistrationResult.PASS);

        pushTokenRepository.save(
                PushToken.create(users.get(0), "device-001", "token-001", DeviceType.ANDROID));

        stageResultCountRebuilder.rebuildAll();

        log.info("=== 시트 기반 더미데이터 준비 완료 (공고 11건 / 전형 41건, 임계값 {}) ===", THRESHOLD);
        log.info("[등록 규칙] p1: 임원면접 이전 전부 5건+ / p2: 직무면접까지 5건+ / 그 외: 발표일 < {} 전형 5건+", TODAY);
        log.info("[오늘({}) 발표 예정] p1 임원면접 (최종) score=65", TODAY);
        log.info("[미래 발표] p2 임원면접 (최종) 2026-07-30 score=65");
        log.info("[등록 없음] p11 삼성전자 SW개발 (발표일 미정)");
    }

    // ── 헬퍼 ──

    private Category category(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("category 테이블에 '" + name + "' 가 없습니다."));
    }

    private Company company(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("company 테이블에 '" + name + "' 가 없습니다."));
    }

    /** 시트에 deadline / expectedAnnouncementDate(공고) 컬럼이 없어 null 로 저장 */
    private Posting posting(String title, String location, Category category, Company company) {
        return postingRepository.save(
                Posting.create(title, null, location, "신입", category, company));
    }

    private record StageSpec(String name, int orderIndex, LocalDate announceDate, int score) {}

    private StageSpec stage(String name, int orderIndex, LocalDate announceDate, int score) {
        return new StageSpec(name, orderIndex, announceDate, score);
    }

    private List<PostingStage> addStages(Posting posting, StageSpec... specs) {
        List<PostingStage> saved = new ArrayList<>();
        for (StageSpec s : specs) {
            PostingStage stage = PostingStage.create(
                    s.name(), s.orderIndex(), s.announceDate(), s.score(), posting);

            // 발표일이 오늘(2026-07-16) 이전이면 이미 발표된 것으로 처리
            if (s.announceDate() != null && s.announceDate().isBefore(TODAY)) {
                stage.markAnnouncedIfAbsent(s.announceDate());
            }

            saved.add(postingStageRepository.save(stage));
        }
        return saved;
    }

    private void register(List<User> users, Posting posting, PostingStage stage,
                          int count, RegistrationResult result) {
        registerResults(users, posting, stage, count, result, 0);
    }

    private void registerResults(List<User> users, Posting posting, PostingStage stage,
                                 int count, RegistrationResult result, int offset) {
        if (offset + count > users.size()) {
            throw new IllegalArgumentException(
                    "offset+count(" + (offset + count) + ") > 유저수(" + users.size() + ").");
        }
        for (int i = 0; i < count; i++) {
            registrationRepository.save(
                    Registration.create(users.get(offset + i), posting, stage, result,
                            List.of(ContactMethod.EMAIL), LocalDateTime.now(), false));
        }
    }
}