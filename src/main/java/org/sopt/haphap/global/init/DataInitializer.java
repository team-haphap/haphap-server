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
import org.sopt.haphap.domain.posting.repository.CompanyImageRepository;
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

@Slf4j
@Component
@Profile("local")
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

        Category marketing = categoryRepository.findByName("마케팅/홍보")
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
                stage("서류", 0, LocalDate.now().minusDays(10), 10),
                stage("코딩 테스트", 1, LocalDate.now().minusDays(2), 10),
                stage("1차 면접", 2, LocalDate.now().plusDays(20), 10),
                stage("최종", 3, LocalDate.now().plusDays(40), 10));
        register(users, a, as.get(0), 25, RegistrationResult.PASS);
        register(users, a, as.get(1), 20, RegistrationResult.PASS);

        Posting b = postingRepository.save(
                Posting.create("B 프론트 신입 공채", LocalDate.now().plusDays(60),
                        "역삼 지사", "프론트엔드 개발자", dev, kakao));
        List<PostingStage> bs = addStages(b,
                stage("서류", 0, LocalDate.now().minusDays(1), 10),
                stage("코딩 테스트", 1, LocalDate.now().plusDays(1), 10),
                stage("1차 면접", 2, LocalDate.now().plusDays(10), 10),
                stage("최종", 3, LocalDate.now().plusDays(20), 10));
        register(users, b, bs.get(0), 18, RegistrationResult.PASS);

        Posting c = postingRepository.save(
                Posting.create("C 기획 신입 공채", LocalDate.now().plusDays(60),
                        "양재 본사", "책임매니저", pm, naver));
        List<PostingStage> cs = addStages(c,
                stage("서류", 0, LocalDate.now().minusDays(3), 10),
                stage("코딩 테스트", 1, LocalDate.now().minusDays(1), 10),
                stage("1차 면접", 2, LocalDate.now().plusDays(5), 10),
                stage("최종", 3, LocalDate.now().plusDays(15), 10));
        register(users, c, cs.get(0), 20, RegistrationResult.PASS);
        register(users, c, cs.get(1), 16, RegistrationResult.PASS);

        Posting d = postingRepository.save(
                Posting.create("D 마케팅 신입 공채", LocalDate.now().plusDays(60),
                        "성수 오피스", "프로덕트 디자이너", marketing, toss));
        List<PostingStage> ds = addStages(d,
                stage("서류", 0, LocalDate.now().plusDays(3), 10),
                stage("최종", 1, LocalDate.now().plusDays(12), 10));
        register(users, d, ds.get(0), 10, RegistrationResult.PENDING);

        Posting e = postingRepository.save(
                Posting.create("E 백엔드 신입 공채", LocalDate.now().plusDays(60),
                        "분당 센터", "백엔드 개발자", dev, naver));
        List<PostingStage> es = addStages(e,
                stage("서류", 0, LocalDate.now().plusDays(7), 10),
                stage("1차 면접", 1, LocalDate.now().plusDays(14), 10));
        register(users, e, es.get(0), 5, RegistrationResult.PASS);

        int[] counts = {35, 32, 28, 15, 12, 8};
        String[] names = {"F", "G", "H", "I", "J", "K"};
        for (int i = 0; i < names.length; i++) {
            Posting p = postingRepository.save(
                    Posting.create(names[i] + " 개발 공채", LocalDate.now().plusDays(60),
                            "본사", "개발자", dev, toss));
            List<PostingStage> ps = addStages(p,
                    stage("서류", 0, LocalDate.now().plusDays(10 + i), 10));
            register(users, p, ps.get(0), counts[i], RegistrationResult.PASS);
        }

        // ============================================================
        // 오늘 발표 예상(/announcements): 발표일=오늘, score 상위 3개
        // ============================================================
        Posting t1 = postingRepository.save(
                Posting.create("오늘발표 하이스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, kakao));
        addStages(t1, stage("1차 면접", 0, LocalDate.now(), 95));

        Posting t2 = postingRepository.save(
                Posting.create("오늘발표 미드스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, toss));
        addStages(t2, stage("서류", 0, LocalDate.now(), 80));

        Posting t3 = postingRepository.save(
                Posting.create("오늘발표 로우스코어 공채", LocalDate.now().plusDays(60),
                        "본사", "기획자", pm, naver));
        addStages(t3, stage("코딩 테스트", 0, LocalDate.now(), 60));

        Posting t4 = postingRepository.save(
                Posting.create("오늘발표 컷오프 공채", LocalDate.now().plusDays(60),
                        "본사", "마케팅", marketing, kakao));
        addStages(t4, stage("최종", 0, LocalDate.now(), 40));

        Posting t5 = postingRepository.save(
                Posting.create("내일발표 공채", LocalDate.now().plusDays(60),
                        "본사", "개발자", dev, naver));
        addStages(t5, stage("서류", 0, LocalDate.now().plusDays(1), 99));

        // ============================================================
        // 공고 상세(/postings/{id}/detail) 검증 전용
        // ============================================================
        Posting detail = postingRepository.save(
                Posting.create("카카오 에너지 태양광 사업 정책기획", LocalDate.now().plusDays(60),
                        "양재 본사", "책임매니저", pm, kakao));
        List<PostingStage> ds2 = addStages(detail,
                stage("서류", 0, LocalDate.now().minusDays(2), 50),
                stage("코딩 테스트", 1, LocalDate.now().plusDays(1), 50),
                stage("1차 면접", 2, LocalDate.now().plusDays(5), 50),
                stage("최종", 3, LocalDate.now().plusDays(12), 50));
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
                            ContactMethod.EMAIL, LocalDateTime.now(), false));
        }
    }
}