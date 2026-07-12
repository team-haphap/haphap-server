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
import org.sopt.haphap.domain.posting.domain.CompanyImage;
import org.sopt.haphap.domain.posting.domain.CompanyImageType;
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
    private final CompanyImageRepository companyImageRepository;
    private final ProfileImageAssigner profileImageAssigner;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
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

        // 배너는 V11 마이그레이션에서 이미 시드하므로 여기서는 만들지 않음

        Category pm = categoryRepository.save(Category.create("기획","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/30b5bdf7-47db-40fa-a22d-0ff942374e9f-pass_planning.png"));
        Category marcketing = categoryRepository.save(Category.create("마케팅/홍보","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/71dedc97-181b-4e40-a1a0-ead86af6eaba-pass_marketing.png"));
        Category hr = categoryRepository.save(Category.create("인사","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/8d220f58-2d06-4c63-b8e0-212c3e55c749-pass_HR.png"));
        Category sales = categoryRepository.save(Category.create("영업","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/101934b8-6318-44b7-acc8-ba13e97f37e0-pass_sales.png"));
        Category dev = categoryRepository.save(Category.create("개발/데이터","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/a0c7e9eb-7ed3-4909-b608-c0c6382873c9-pass_data.png"));
        Category finance = categoryRepository.save(Category.create("금융/보험","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/pass-cards/63534f4c-a491-4211-adb9-a36c67a9ae5f-pass_finance.png"));

        Company toss = companyRepository.save(
                Company.create("토스", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/c72ed9c2-b386-4641-8947-5314a5130d75-pass_logo_toss.png"));
        saveCompanyImages(toss,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/8c860bbc-478d-4219-8220-2e3517bdbd83-home_toss.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/a69f9d85-da1a-4be3-b1ad-95746819bf0a-list_toss.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/31395569-4ccc-484f-aa06-addabac8ba9a-detail_toss.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/aa0ecc80-efe2-4b9a-9db8-3634d407ab47-home_toss.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/f470a42a-649b-4408-a845-4270ba7434c0-calender_toss.png");

        Company kakao = companyRepository.save(
                Company.create("카카오", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/17164eae-eae2-4400-8563-a62ad4421bef-pass_logo_kakao.png"));
        saveCompanyImages(kakao,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/2ac9c2ed-f1f4-46b8-a19f-dd78d965f454-home_kakao.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/73d28707-e13d-46da-bf87-a6dce00c372b-list_kakao.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/aa53dfbe-1dbb-4307-acae-51b755b043a7-detail_kakao.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/b93d8d83-56fc-4138-8e19-c66cd5b17b1d-home_kakao.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/aac7b3d6-42cb-4499-be3a-e0915d9c5e04-calender_kakao.png");

        Company naver = companyRepository.save(
                Company.create("네이버", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/970a1e17-211b-4f12-a744-c002fbb273f2-pass_logo_naver.png"));
        saveCompanyImages(naver,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3b81705a-f129-4522-afe9-42f928541e96-home_naver.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/4c8ca56e-59a9-4c94-b2ef-bafcbf3f648e-list_naver.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/90734025-1a97-4151-945e-b33cdb31cfaf-detail_naver.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/7233e784-00b4-4144-b6ea-e2685351959f-home_naver.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/72d81b59-15fc-479d-bf83-8babdc5fa12b-calender_naver.png");

        Company amore = companyRepository.save(
                Company.create("아모레퍼시픽", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/e14daa68-5389-48e3-8888-ec281a7fee7c-pass_logo_amore.png"));
        saveCompanyImages(amore,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/f36c4c11-10eb-4511-8370-a54e696629c6-home_amore.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3116c871-7ce3-4a76-b351-b4dd7f60212d-list_amore.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5f38a277-9f4f-4c51-ad41-3d4c83bdcfe7-detail_amore.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/2d3e6ab0-11f0-4891-9971-c4685603a535-home_amore.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/82d758eb-1c80-4361-b060-62b9bdc89bdc-calender_amore.png");

        Company hyundai = companyRepository.save(
                Company.create("현대자동차", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/e61e90d6-0b56-417c-bdb4-8fb8fa9294f0-pass_logo_hyundai.png"));
        saveCompanyImages(hyundai,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5855cf81-0132-4566-adba-bb1ddb97378c-home_hyundai.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/6ac1f400-3d6a-4e51-b4a8-ba43abdbc576-list_hyundai.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/82b19c59-c8df-456c-b78a-d0f74e9dffdc-detail_hyundai.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/6324da13-ac26-449d-9be9-2bb9c23f55bc-home_hyundai.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/218a8bd2-dd0f-41e2-a047-13d4d3d551aa-calender_hyundai.png");

        Company lg = companyRepository.save(
                Company.create("LG", null, null, null,
                        "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/e9831cda-a4bb-4db5-8be6-0513e164464a-pass_logo_lg.png"));
        saveCompanyImages(lg,
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/5f49eca0-59c9-4f26-a9b3-e8790a8f1606-home_lg.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/80fc1ddf-6f99-4f9b-bd7b-cd33e51d38b1-list_lg.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/images/3fad0aab-3de5-47a8-80b7-14c6b782dfa0-detail_lg.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/25035d03-1558-4c9b-adf7-5615d9d1f6af-home_lg.png",
                "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/logo-images/01bbc81d-eba3-454d-af5d-e11fa899452a-calender_lg.png");

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
                        "성수 오피스", "프로덕트 디자이너", marcketing, toss));
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
                        "본사", "마케팅", marcketing, kakao));
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

    private void saveCompanyImages(Company company, String popular, String listing, String detail,
                                   String todayLogo, String calendarLogo) {
        companyImageRepository.save(CompanyImage.create(company, CompanyImageType.POPULAR, popular));
        companyImageRepository.save(CompanyImage.create(company, CompanyImageType.LISTING, listing));
        companyImageRepository.save(CompanyImage.create(company, CompanyImageType.DETAIL, detail));
        companyImageRepository.save(CompanyImage.create(company, CompanyImageType.TODAY_LOGO, todayLogo));
        companyImageRepository.save(CompanyImage.create(company, CompanyImageType.CALENDAR_LOGO, calendarLogo));
    }
}