package org.sopt.haphap.global.init;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.alram.domain.DeviceType;
import org.sopt.haphap.domain.alram.domain.PushToken;
import org.sopt.haphap.domain.alram.repository.PushTokenRepository;
import org.sopt.haphap.domain.banner.domain.Banner;
import org.sopt.haphap.domain.banner.repository.BannerRepository;
import org.sopt.haphap.domain.posting.service.aggregate.StageResultCountRebuilder;
import org.sopt.haphap.domain.registration.domain.ContactMethod;
import org.sopt.haphap.domain.registration.domain.Registration;
import org.sopt.haphap.domain.registration.domain.RegistrationResult;
import org.sopt.haphap.domain.registration.repository.RegistrationRepository;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.domain.user.entity.User;
import org.sopt.haphap.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.sopt.haphap.domain.posting.domain.Category;
import org.sopt.haphap.domain.posting.domain.Company;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.domain.PostingStage;
import org.sopt.haphap.domain.posting.repository.CategoryRepository;
import org.sopt.haphap.domain.posting.repository.CompanyRepository;
import org.sopt.haphap.domain.posting.repository.PostingRepository;
import org.sopt.haphap.domain.posting.repository.PostingStageRepository;


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
    private final BannerRepository bannerRepository;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            log.info("=== 초기 데이터가 이미 존재하여 DataInitializer를 건너뜁니다 ===");
            return;
        }

        // ── 유저 40명 (프로필 이미지 포함) ──
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            String profileImageUrl =
                    PROFILE_IMAGES.get(random.nextInt(PROFILE_IMAGES.size()));

            users.add(userRepository.save(User.builder()
                    .anonymousName("익명" + i)
                    .name("유저" + i)
                    .email("user" + i + "@test.com")
                    .provider(Provider.KAKAO)
                    .providerId("user-" + i)
                    .profileImageUrl(profileImageUrl)
                    .build()));
        }
        bannerRepository.save(Banner.create("https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/banners/ecc92117-8479-45da-a164-05525cde7cba-img_card_banner-1.pdf",0));
        bannerRepository.save(Banner.create("https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/banners/495b46d9-8541-4c4d-8eee-2a2adc0870c2-img_card_banner-2.pdf",1));
        bannerRepository.save(Banner.create("https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/banners/7e7209ca-e01d-457b-a4fb-1d09da35fcf5-img_card_banner-3.pdf",2));
        bannerRepository.save(Banner.create("https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/banners/3b9936bc-7ffe-4641-9dba-ad2360ba60da-img_card_banner-4.pdf",3));
        bannerRepository.save(Banner.create("https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/banners/7f6b3412-4691-411b-81eb-b6b8856818a2-img_card_banner.pdf",4));


        Category pm = categoryRepository.save(Category.create("기획","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/88d4ce84-03ba-45a4-881d-4a76ff56bc3e-기획.pdf"));
        Category marcketing = categoryRepository.save(Category.create("마케팅/홍보","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/d19b8bae-8110-472c-84dd-fdfe9ef00fa6-마케팅_홍보.pdf"));
        Category hr = categoryRepository.save(Category.create("인사","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/77a00bf1-5d13-41c5-9353-bc7e44d050cc-인사.pdf"));
        Category sales = categoryRepository.save(Category.create("영업","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/94e68943-c52e-4c64-9f3b-87c36dd40022-영업.pdf"));
        Category dev = categoryRepository.save(Category.create("개발/데이터","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/77eb73fe-7c73-4200-9203-175700e1527f-개발_ 데이터.pdf"));
        Category finance = categoryRepository.save(Category.create("금융/보험","https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/card-logos/703fccc1-e101-44d5-a269-44b0ede7c90c-금융_보험.pdf"));

        //todo: 오늘발표로고/캘린더 로고 분리한다음 새로 저장하기!!
        Company toss = companyRepository.save(
                Company.create("토스", "토스는 금융을 쉽고 간편하게 만듭니다.","logo.com1", "https://example.com/toss.png","card1.png"));
        Company kakao = companyRepository.save(
                Company.create("카카오", "사람과 기술로 더 나은 세상을.","logo.com2", "https://example.com/kakao.png","card2.png"));
        Company naver = companyRepository.save(
                Company.create("네이버", "연결의 힘을 믿습니다.","logo.com3", "https://example.com/naver.png","card3.png"));


        // ============================================================
        // 기존 시나리오: 인기순(/postings) · 발표일순(/postings/all)
        //   Posting.create(title, deadline, location, position, category, company)
        //   stage(name, orderIndex, 발표일, score)
        // ============================================================

        // [A] 인기순O. 서류25·코테20 PASS(≥15)→현재진행=코테, next=1차(+20)

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

        // [B] 인기순O. 서류18 PASS(≥15), 코테0→현재진행=서류, next=코테(+1)

        Posting b = postingRepository.save(
                Posting.create("B 프론트 신입 공채", LocalDate.now().plusDays(60),
                        "역삼 지사", "프론트엔드 개발자", dev, kakao));


        List<PostingStage> bs = addStages(b,
                stage("서류", 0, LocalDate.now().minusDays(1), 10),
                stage("코딩 테스트", 1, LocalDate.now().plusDays(1), 10),
                stage("1차 면접", 2, LocalDate.now().plusDays(10), 10),
                stage("최종", 3, LocalDate.now().plusDays(20), 10));
        register(users, b, bs.get(0), 18, RegistrationResult.PASS);

        // [C] 전체O(인기순은 SQL로 코테 밀어야 제외). 서류20·코테16 PASS→현재진행=코테, next=1차(+5)

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

        // [D] 인기순X(PENDING만)/전체O. next=서류(+3)

        Posting d = postingRepository.save(
                Posting.create("D 마케팅 신입 공채", LocalDate.now().plusDays(60),
                        "성수 오피스", "프로덕트 디자이너", marcketing, toss));

        List<PostingStage> ds = addStages(d,
                stage("서류", 0, LocalDate.now().plusDays(3), 10),
                stage("최종", 1, LocalDate.now().plusDays(12), 10));
        register(users, d, ds.get(0), 10, RegistrationResult.PENDING);

        // [E] 인기순O. 서류5 PASS(<15)→현재진행=서류, next=서류(+7)

        Posting e = postingRepository.save(
                Posting.create("E 백엔드 신입 공채", LocalDate.now().plusDays(60),
                        "분당 센터", "백엔드 개발자", dev, naver));

        List<PostingStage> es = addStages(e,
                stage("서류", 0, LocalDate.now().plusDays(7), 10),
                stage("1차 면접", 1, LocalDate.now().plusDays(14), 10));
        register(users, e, es.get(0), 5, RegistrationResult.PASS);

        // [F~K] 인기순 8개 제한 검증
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
        //   여러 유저가 여러 전형에 등록 + 결과 다양(PASS/FAIL/PENDING)
        //   → registeredCount(중복제거 유저수), 최근참여 4명, 실시간 제보, currentState 확인
        //   서류18·코테16 PASS(≥15) → 현재진행=코테 ("코테 진행 중" 상당)
        // ============================================================
        Posting detail = postingRepository.save(

        Posting.create("카카오 에너지 태양광 사업 정책기획", LocalDate.now().plusDays(60),
                        "양재 본사", "책임매니저", pm, kakao));


        List<PostingStage> ds2 = addStages(detail,
                stage("서류", 0, LocalDate.now().minusDays(2), 50),
                stage("코딩 테스트", 1, LocalDate.now().plusDays(1), 50),
                stage("1차 면접", 2, LocalDate.now().plusDays(5), 50),
                stage("최종", 3, LocalDate.now().plusDays(12), 50));
        // 서류: 다양한 결과로 18명 (현재진행 판정은 PASS+FAIL≥15)
        registerResults(users, detail, ds2.get(0), 12, RegistrationResult.PASS);
        registerResults(users, detail, ds2.get(0), 4, RegistrationResult.FAIL, 12);   // 유저 13~16
        registerResults(users, detail, ds2.get(0), 4, RegistrationResult.PENDING, 16);// 유저 17~20
        // 코테: PASS 16명 → 코테도 ≥15 → 현재진행=코테
        registerResults(users, detail, ds2.get(1), 16, RegistrationResult.PASS);
        // 1차: 소수 등록 (진행 전형 아님)
        registerResults(users, detail, ds2.get(2), 3, RegistrationResult.PENDING);

        pushTokenRepository.save(
                PushToken.create(users.get(0), "device-001", "token-001", DeviceType.ANDROID));

        // 집계 테이블 초기화
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

    // 유저 0번부터 count명 등록 (기존 시나리오용)
    private void register(List<User> users, Posting posting, PostingStage stage,
                          int count, RegistrationResult result) {
        registerResults(users, posting, stage, count, result, 0);
    }

    private void registerResults(List<User> users, Posting posting, PostingStage stage,
                                 int count, RegistrationResult result) {
        registerResults(users, posting, stage, count, result, 0);
    }

    // 유저 offset번부터 count명을 지정 결과로 등록 (서로 다른 유저)
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
    private static final List<String> PROFILE_IMAGES = List.of(
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/d89f1451-9eee-4804-8abe-7bbfc82c1a91-img_10.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/a4f26e5b-af33-446d-90ae-9d031dfad31c-img_9.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/dffb023e-8fc3-4b51-ba66-64b7e7058494-img_8.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/6e6e6bb8-a2b4-4d0b-8ad9-f92ce01f5fb4-img_7.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/a487346f-53b6-4668-814d-897da455ded4-img_6.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/b2884404-c166-431b-bbdb-a4c976a072f9-img_5.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/679bb4c2-8d51-4b6d-9f32-4b658eb7922b-img_4.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/71401700-2017-4457-b298-a664da59dc50-img_3.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/6dc58d49-3849-4bf0-999d-1e890bedd9d0-img_2.pdf",
            "https://haphap-images-654801597877-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile-images/5d37a975-9b55-4033-8b14-c63ce30de1db-img_1.pdf"
    );

    private final Random random = new Random();
}