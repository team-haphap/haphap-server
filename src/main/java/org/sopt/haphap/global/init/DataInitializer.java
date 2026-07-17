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

import org.springframework.stereotype.Component;

/**
 * 스프레드시트(공고 50건 / 전형 175건) 기반 더미데이터 초기화.
 *
 * 핵심 규칙 (하드코딩된 등록 건수 대신 코드로 표현)
 *  - announcedDate : expectedAnnouncementDate < TODAY(2026-07-16) 인 전형만 발표 처리
 *  - Registration  : 발표된 전형에만 등록 생성, 항상 MIN_REGISTRATIONS(5)건 이상
 *                    미발표(오늘 이후) 전형은 등록 없음
 *  - 건수는 공고 제목 해시 기반이라 실행마다 동일하며, orderIndex 가 올라갈수록 줄어드는 깔때기 형태
 *
 * 이 규칙만으로 기존 요구사항이 모두 충족됨
 *  - posting 1 : 임원면접(최종)이 7/16 = 오늘 → 미발표. 그 이전 전형은 전부 5건 이상
 *  - posting 2 : 임원면접(최종)이 7/30 = 미래 → 미발표. 직무면접까지 전부 5건 이상
 *
 * orderIndex 는 시트와 동일하게 1부터 시작.
 */
@Slf4j
@Component
//@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int THRESHOLD = 15;

    /** 시트 기준 "오늘". 발표 여부 / 등록 여부 판단 기준일 */
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 17);

    /** 발표된 전형이 보장받는 최소 등록 건수 */
    private static final int MIN_REGISTRATIONS = 5;

    private static final int USER_COUNT = 40;

    private final UserRepository userRepository;
    private final PostingRepository postingRepository;
    private final PushTokenRepository pushTokenRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final PostingStageRepository postingStageRepository;
    private final RegistrationRepository registrationRepository;
    private final StageResultCountRebuilder stageResultCountRebuilder;
    private final ProfileImageAssigner profileImageAssigner;

    private List<User> users;
    private int postingCount = 0;
    private int stageCount = 0;
    private int announcedCount = 0;
    private int registrationCount = 0;

    @Override
    public void run(String... args) {

        if (postingRepository.count() > 0) {
            log.info("=== 초기 데이터가 이미 존재하여 DataInitializer를 건너뜁니다 ===");
            return;
        }

        users = new ArrayList<>();
        for (int i = 1; i <= USER_COUNT; i++) {
            users.add(userRepository.save(User.builder()
                    .anonymousName("익명" + i)
                    .name("유저" + i)
                    .email("user" + i + "@test.com")
                    .provider(Provider.KAKAO)
                    .providerId("user-" + i)
                    .profileImageUrl(profileImageAssigner.assign())
                    .build()));
        }

        Category pm = category("기획");
        Category design = category("디자인");
        Category hr = category("인사");
        Category sales = category("영업");
        Category dev = category("개발/데이터");
        Category finance = category("금융/보험");

        Company toss = company("토스");
        Company kakao = company("카카오");
        Company naver = company("네이버");
        Company amore = company("아모레퍼시픽");
        Company hyundai = company("현대자동차");
        Company lgHnh = company("LG생활건강");   // ★ company 테이블에 미리 추가 필요

        // ===== 현대자동차 =====
        seed("자율주행개발 - 2026 3월 신입 채용", "남양, 판교", dev, hyundai,
                stage("서류", 1, d(2026, 6, 5), 87),
                stage("인적성검사", 2, d(2026, 6, 19), 23),
                stage("직무면접", 3, d(2026, 7, 17), 44), //Todo: 7/18로
                stage("임원면접 (최종)", 4, d(2026, 8, 3), 65));

        seed("차량개발 - 2026 3월 신입 채용", "남양", dev, hyundai,
                stage("서류", 1, d(2026, 6, 5), 34),
                stage("인적성검사", 2, d(2026, 6, 24), 80),
                stage("코딩테스트", 3, d(2026, 7, 7), 54),
                stage("직무면접", 4, d(2026, 7, 17), 23),
                stage("임원면접 (최종)", 5, d(2026, 8, 7), 65));  //Todo: 7/18로

        seed("경영기획 - 2026 3월 신입 채용", "양재", pm, hyundai,
                stage("서류", 1, d(2026, 6, 5), 66),
                stage("인적성검사", 2, d(2026, 6, 29), 23),
                stage("직무면접", 3, d(2026, 7, 17), 19), //Todo: 7/18로
                stage("임원면접 (최종)", 4, d(2026, 8, 7), 70));

        seed("신사업 전략 기획 - 2026 3월 신입 채용", "양재", pm, hyundai,
                stage("서류", 1, d(2026, 6, 5), 86),
                stage("인적성검사", 2, d(2026, 6, 29), 58),
                stage("직무면접", 3, d(2026, 7, 17), 34), //Todo: 7/18로
                stage("임원면접 (최종)", 4, d(2026, 8, 7), 91));

        seed("HR - 2026 3월 신입 채용", "양재, 울산", hr, hyundai,
                stage("서류", 1, d(2026, 6, 5), 10),
                stage("인적성검사", 2, d(2026, 6, 29), 45),
                stage("직무면접", 3, d(2026, 7, 17), 76), //Todo: 7/18로
                stage("임원면접 (최종)", 4, d(2026, 8, 7), 34));

        // ===== 네이버 / 카카오 / 아모레 (기존) =====
        seed("Product Design - 2026 신입 공채", "NAVER 1784", design, naver,   // 공고명 "2025" → 2025
                stage("서류", 1, d(2025, 4, 8), 90),
                stage("1차 면접", 2, d(2025, 4, 30), 45),
                stage("챌린지", 3, d(2025, 5, 29), 12),
                stage("2차 면접 (최종)", 4, d(2025, 6, 27), 45));

        seed("AI 서비스 개발 - 2026 신입크루 공채", "판교 오피스", dev, kakao,   // 11~12월 → 2025 가정
                stage("서류", 1, d(2025, 11, 10), 67),
                stage("1차 면접", 2, d(2025, 11, 24), 23),
                stage("2차 면접 (최종)", 3, d(2025, 12, 17), 12));

        seed("AI 서비스 운영 - 2026 신입크루 공채", "판교 오피스", dev, kakao,
                stage("서류", 1, d(2025, 11, 10), 67),
                stage("1차 면접", 2, d(2025, 11, 24), 34),
                stage("2차 면접 (최종)", 3, d(2025, 12, 17), 23));

        seed("공간디자인 - 2026 상반기 신입사원 수시채용", "본사", design, amore,
                stage("서류", 1, d(2026, 4, 20), 15),
                stage("AI역량/영어면접", 2, d(2026, 4, 29), 76),
                stage("1차 면접", 3, d(2026, 5, 15), 45),
                stage("2차 면접 (최종)", 4, d(2026, 6, 24), 63));

        seed("생산시스템 엔지니어 - 2026 상반기 신입사원 수시채용", "본사", dev, amore,
                stage("서류", 1, d(2026, 4, 20), 85),
                stage("AI역량/영어면접", 2, d(2026, 4, 29), 95),
                stage("1차 면접", 3, d(2026, 5, 15), 24),
                stage("2차 면접 (최종)", 4, d(2026, 6, 24), 64));

        // ===== LG생활건강 =====
        // ※ 시트상 11번의 회사가 삼성전자 → LG생활건강 으로 바뀌었음 (공고명/근무지는 그대로)
        seed("SW개발 - 2026년 상반기 3급 신입사원 채용 공고", "화성", dev, lgHnh,
                stage("지원서 접수", 1, d(2026, 3, 30), 36),
                stage("직무 적합성 평가", 2, d(2026, 4, 6), 59),
                stage("직무 적성 검사", 3, d(2026, 4, 25), 25),
                stage("면접 (최종)", 4, d(2026, 5, 10), 88));

        seed("2026년 하반기 신입사원 채용 (마케팅 / 영업)", "종로구", sales, lgHnh,  // 공고명 "2025년" → 2025
                stage("서류", 1, d(2025, 5, 27), 22),
                stage("인적성 검사", 2, d(2025, 6, 11), 46),
                stage("1차 면접", 3, d(2025, 6, 20), 39),
                stage("2차 면접", 4, d(2025, 7, 1), 65),
                stage("인턴십 전형 (최종)", 5, d(2025, 7, 14), 43));

        // ===== 카카오 (신규) =====
        seed("LLM Research Engineer (Pre-training) 신입 채용", "판교 오피스", dev, kakao,  // 전부 미래
                stage("서류", 1, d(2026, 9, 4), 34),
                stage("코딩테스트", 2, d(2026, 9, 18), 58),
                stage("1차 면접", 3, d(2026, 10, 4), 78),
                stage("2차 면접 (최종)", 4, d(2026, 10, 13), 63));

        seed("카카오쇼핑(톡딜/선물하기) 판매자 성장지원 담당자 신입 채용", "판교 오피스", pm, kakao,
                stage("서류", 1, d(2026, 3, 5), 55),
                stage("1차 면접", 2, d(2026, 3, 22), 26),
                stage("2차 면접 (최종)", 3, d(2026, 4, 3), 49));

        // ===== 네이버 (신규) =====
        seed("검색 서비스 기획 - 2026 팀네이버 신입 공채", "NAVER 1784", pm, naver,
                stage("서류", 1, d(2025, 7, 9), 65),
                stage("프로덕트 디벨롭 인터뷰", 2, d(2025, 7, 18), 77),
                stage("챌린지 전형 & 종합 역량 인터뷰 (최종)", 3, d(2025, 8, 3), 94));

        seed("백엔드 서버 개발 신입 채용", "NAVER 1784", dev, naver,
                stage("서류", 1, d(2026, 7, 17), 38),
                stage("코딩테스트", 2, d(2026, 8, 4), 56),
                stage("프리 인터뷰", 3, d(2026, 8, 20), 87),
                stage("Culture-Fit 인터뷰", 4, d(2026, 9, 3), 34),
                stage("실무 인터뷰 (최종)", 5, d(2026, 9, 18), 23));


        seed("IT 보안 기술 담당 신입 채용", "NAVER 1784", dev, naver,
                stage("서류 전형", 1, d(2026, 7, 16), 17),
                stage("1차 인터뷰", 2, d(2026, 7, 30), 62),
                stage("2차 인터뷰 (최종)", 3, d(2026, 8, 9), 59));

        seed("쇼핑 신사업 개발 담당 신입 채용", "NAVER 1784", sales, naver,
                stage("서류전형&기업문화 적합도 검사", 1, d(2026, 3, 22), 28),
                stage("직무 인터뷰", 2, d(2026, 4, 7), 57),
                stage("종합 인터뷰 (최종)", 3, d(2026, 4, 24), 78));

        seed("Embodied AI Research Engineer 신입 채용", "NAVER 1784", dev, naver,  // 전부 미래
                stage("서류 전형", 1, d(2026, 11, 13), 53),
                stage("전화 면접", 2, d(2026, 11, 21), 31),
                stage("1차 면접", 3, d(2026, 11, 30), 67),
                stage("2차 면접 (최종)", 4, d(2026, 12, 8), 48));

        seed("헬스케어 산업 리서치 신입 채용", "NAVER 1784", sales, naver,  // 전부 미래
                stage("서류 전형", 1, d(2026, 8, 7), 22),
                stage("직무 인터뷰 (최종)", 2, d(2026, 8, 20), 76));

        // ===== 토스 =====
        seed("Security Research Specialist 신입 채용", "강남", dev, toss,  // 전부 오늘 이후
                stage("서류 전형", 1, d(2026, 7, 16), 69),
                stage("직무 인터뷰", 2, d(2026, 8, 1), 43),
                stage("문화적합성 인터뷰 (최종)", 3, d(2026, 8, 9), 56));

        seed("여신 상품 Manager 신입 채용", "강남", sales, toss,  // 전부 오늘 이후
                stage("서류 전형", 1, d(2026, 7, 16), 45),
                stage("직무 인터뷰", 2, d(2026, 7, 29), 63),
                stage("문화적합성 인터뷰", 3, d(2026, 8, 26), 24),
                stage("레퍼런스 체크 (최종)", 4, d(2026, 8, 29), 87));

        seed("Interaction Designer 신입 채용", "강남", design, toss,
                stage("과제 제출", 1, d(2026, 4, 1), 44),
                stage("직무 인터뷰", 2, d(2026, 4, 12), 23),
                stage("문화적합성 인터뷰", 3, d(2026, 4, 23), 54),
                stage("레퍼런스 체크 (최종)", 4, d(2026, 5, 2), 39));

        seed("IT admin 신입 채용", "강남", dev, toss,
                stage("서류 전형", 1, d(2026, 1, 3), 46),
                stage("직무 인터뷰", 2, d(2026, 1, 15), 12),
                stage("문화적합성 인터뷰 (최종)", 3, d(2026, 1, 24), 76));

        seed("프론트 개발자 신입 채용", "강남", dev, toss,  // 전부 미래
                stage("서류 전형", 1, d(2026, 9, 23), 85),
                stage("직무 인터뷰", 2, d(2026, 9, 30), 76),
                stage("문화적합성 인터뷰", 3, d(2026, 10, 8), 54),
                stage("레퍼런스 체크 (최종)", 4, d(2026, 10, 13), 23));

        seed("브랜드 디자이너 신입 채용", "강남", design, toss,
                stage("서류 전형", 1, d(2026, 3, 21), 66),
                stage("직무 인터뷰", 2, d(2026, 4, 5), 23),
                stage("문화적합성 인터뷰 ", 3, d(2026, 4, 14), 56),
                stage("레퍼런스 체크 (최종)", 4, d(2026, 4, 20), 79));

        seed("금융사기대응팀 신입 채용", "강남", finance, toss,  // 1~2번 발표 / 3번 미발표
                stage("서류 전형", 1, d(2026, 7, 5), 64),
                stage("직무 인터뷰", 2, d(2026, 7, 13), 67),
                stage("문화적합성 인터뷰 (최종)", 3, d(2026, 7, 21), 89));

        seed("ML Data Assistant 신입 채용", "강남", dev, toss,  // 전부 미래
                stage("서류 전형", 1, d(2026, 7, 2), 56),
                stage("코딩 테스트", 2, d(2026, 7, 17), 87),
                stage("직무 인터뷰", 3, d(2026, 8, 10), 45),
                stage("문화적합성 인터뷰 (최종)", 4, d(2026, 8, 28), 63));

        // ===== 카카오 (추가) =====
        seed("ESG 경영 업무 신입 채용", "판교 오피스", sales, kakao,
                stage("서류 전형", 1, d(2026, 4, 18), 42),
                stage("1차 면접", 2, d(2026, 4, 25), 61),
                stage("2차 면접 (최종)", 3, d(2026, 5, 3), 39));

        seed("카카오프렌즈 디자이너 신입 채용", "판교 오피스", design, kakao,
                stage("서류 전형", 1, d(2026, 2, 2), 54),
                stage("1차 면접", 2, d(2026, 2, 12), 67),
                stage("2차 면접 (최종)", 3, d(2026, 2, 23), 43));

        seed("이용자보호 업무 신입 채용", "판교 오피스", dev, kakao,  // 1~2번 발표 / 3~4번 미발표
                stage("서류", 1, d(2026, 7, 7), 44),
                stage("코딩 테스트", 2, d(2026, 7, 17), 45),
                stage("직무 적합성 평가", 3, d(2026, 8, 3), 64),
                stage("직무 적성 검사", 4, d(2026, 8, 24), 21),
                stage("면접 (최종)", 5, d(2026, 8, 30), 34));//TODO. 수정함

        seed("부동산자산관리 신입 채용", "판교 오피스", finance, kakao,  // 전부 미래
                stage("서류 전형", 1, d(2026, 7, 18), 13),
                stage("1차 면접", 2, d(2026, 8, 10), 28),
                stage("2차 면접 (최종)", 3, d(2026, 8, 28), 47));

        seed("노사 전략 및 기획 담당자 신입 채용", "판교 오피스", pm, kakao,
                stage("서류 전형", 1, d(2026, 5, 4), 25),
                stage("1차 면접", 2, d(2026, 5, 11), 57),
                stage("2차 면접 (최종)", 3, d(2026, 5, 22), 78));

        seed("머신러닝 엔지니어 신입 채용", "판교 오피스", dev, kakao,
                stage("지원서 접수", 1, d(2026, 6, 23), 21),
                stage("직무 적합성 평가", 2, d(2026, 6, 30), 37),
                stage("직무 적성 검사", 3, d(2026, 7, 5), 87),
                stage("면접 (최종)", 4, d(2026, 7, 13), 63));

        seed("쇼핑라이브 상품 운영 신입 채용", "판교 오피스", sales, kakao,  // 전부 미래
                stage("서류 전형", 1, d(2026, 12, 1), 32),
                stage("1차 면접", 2, d(2026, 12, 12), 44),
                stage("2차 면접 (최종)", 3, d(2026, 12, 23), 62));

        seed("인재영입 운영 신입 채용", "판교 오피스", hr, kakao,
                stage("서류 전형", 1, d(2026, 3, 2), 48),
                stage("1차 면접", 2, d(2026, 3, 13), 36),
                stage("2차 면접 (최종)", 3, d(2026, 3, 22), 27));

        // ===== 아모레퍼시픽 (추가) =====
        seed("2026 상반기 신입사원 수시채용", "용산구", pm, amore,
                stage("서류 접수", 1, d(2026, 6, 4), 35),
                stage("영어 면접", 2, d(2026, 6, 12), 22),
                stage("1차 면접", 3, d(2026, 6, 21), 67),
                stage("2차 면접 (최종)", 4, d(2026, 7, 1), 86));

        seed("제품디자인 신입사원 채용", "용산구", design, amore,
                stage("서류 접수", 1, d(2026, 5, 6), 25),
                stage("인성 검사", 2, d(2026, 5, 13), 57),
                stage("1차 면접", 3, d(2026, 5, 19), 78),
                stage("2차 면접 (최종)", 4, d(2026, 5, 28), 21));

        seed("건강기능식품 제조/지원 신입 채용", "용산구", pm, amore,  // 전부 미래
                stage("서류 접수", 1, d(2026, 9, 2), 14),
                stage("인성 검사", 2, d(2026, 9, 11), 36),
                stage("1차 면접", 3, d(2026, 9, 18), 54),
                stage("2차 면접 (최종)", 4, d(2026, 9, 27), 71));

        seed("려(呂)BM팀 디자이너 신입 채용", "용산구", design, amore,  // 1~2번 발표 / 3번 미발표
                stage("서류 전형", 1, d(2026, 7, 3), 51),
                stage("1차 면접", 2, d(2026, 7, 10), 42),
                stage("2차 면접 (최종)", 3, d(2026, 7, 17), 69));

        seed("백엔드 개발자 신입 채용", "용산구", dev, amore,  // 1~3번 발표 / 4번 미발표
                stage("서류 전형", 1, d(2026, 7, 2), 22),
                stage("AI역량검사", 2, d(2026, 7, 10), 46),
                stage("1차 면접", 3, d(2026, 7, 15), 39),
                stage("2차 면접 (최종)", 4, d(2026, 7, 22), 65));

        seed("연구개발팀 전임직 신입 채용", "용산구", dev, amore,
                stage("서류 접수", 1, d(2026, 3, 3), 18),
                stage("인성 검사", 2, d(2026, 3, 12), 73),
                stage("1차 면접", 3, d(2026, 3, 20), 82),
                stage("2차 면접 (최종)", 4, d(2026, 3, 27), 47));

        seed("디지털컨텐츠 제작 신입 채용", "용산구", pm, amore,
                stage("서류 전형", 1, d(2026, 7, 4), 34),
                stage("1차 면접", 2, d(2026, 7, 17), 21),
                stage("2차 면접 (최종)", 3, d(2026, 8, 20), 65));

        // ===== LG생활건강 (추가) =====
        seed("영상컨텐츠 기획 신입 채용", "화성", pm, lgHnh,
                stage("서류 전형", 1, d(2026, 4, 6), 42),
                stage("1차 면접", 2, d(2026, 4, 18), 23),
                stage("2차 면접 (최종)", 3, d(2026, 4, 29), 75));

        seed("AX 신입 채용", "화성", dev, lgHnh,  // 전부 미래
                stage("서류 접수", 1, d(2026, 6, 19), 43),
                stage("인성 검사", 2, d(2026, 7, 3), 24),
                stage("코딩 테스트", 3, d(2026, 7, 17), 27),
                stage("1차 면접", 4, d(2026, 7, 31), 15),
                stage("2차 면접 (최종)", 5, d(2026, 8, 14), 62));

        seed("노경담당자 신입 채용", "화성", pm, lgHnh,
                stage("서류 전형", 1, d(2026, 5, 15), 33),
                stage("1차 면접", 2, d(2026, 5, 22), 45),
                stage("2차 면접 (최종)", 3, d(2026, 6, 1), 79));

        seed("재경 신입사원 채용", "화성", finance, lgHnh,
                stage("서류 전형", 1, d(2026, 5, 10), 42),
                stage("인적성 검사", 2, d(2026, 5, 17), 36),
                stage("면접 (최종)", 3, d(2026, 5, 28), 65));

        seed("디지털컨텐츠 소셜크루 신입 채용", "화성", design, lgHnh,
                stage("서류 전형", 1, d(2026, 7, 5), 26),
                stage("인적성 검사", 2, d(2026, 7, 18), 88),
                stage("면접 (최종)", 3, d(2026, 8, 11), 47));

        // ===== 현대자동차 (추가) =====
        seed("모빌리티 기술인력 신입 채용", "화성", dev, hyundai,  // 1~2번 발표 / 3번 미발표
                stage("서류 전형", 1, d(2026, 7, 1), 54),
                stage("인적성 검사", 2, d(2026, 7, 9), 24),
                stage("면접 (최종)", 3, d(2026, 7, 17), 67));

        seed("연구소 배터리 기술인력 신입 채용", "화성", dev, hyundai,  // 1~2번 발표 / 3~4번 미발표
                stage("서류 접수", 1, d(2026, 7, 3), 41),
                stage("인적성 검사", 2, d(2026, 7, 10), 32),
                stage("1차 면접", 3, d(2026, 7, 17), 68),
                stage("2차 면접 (최종)", 4, d(2026, 7, 23), 15));

        pushTokenRepository.save(
                PushToken.create(users.get(0), "device-001", "token-001", DeviceType.ANDROID));

        stageResultCountRebuilder.rebuildAll();

        log.info("=== 시트 기반 더미데이터 준비 완료 (임계값 {}) ===", THRESHOLD);
        log.info("공고 {}건 / 전형 {}건 (발표 {}건, 미발표 {}건) / 등록 {}건",
                postingCount, stageCount, announcedCount, stageCount - announcedCount, registrationCount);
        log.info("기준일 TODAY={} · 발표일 < TODAY 인 전형만 announcedDate 기록 + 등록 {}건 이상",
                TODAY, MIN_REGISTRATIONS);
    }

    // ── 조회 헬퍼 ──

    private Category category(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("category 테이블에 '" + name + "' 가 없습니다."));
    }

    private Company company(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("company 테이블에 '" + name + "' 가 없습니다."));
    }

    private static LocalDate d(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    private record StageSpec(String name, int orderIndex, LocalDate announceDate, int score) {}

    private StageSpec stage(String name, int orderIndex, LocalDate announceDate, int score) {
        return new StageSpec(name, orderIndex, announceDate, score);
    }

    // ── 생성 ──

    /** 공고 1건 + 전형 + 등록을 한 번에 생성. 시트에 deadline 컬럼이 없어 null 로 저장 */
    private void seed(String title, String location, Category category, Company company, StageSpec... specs) {
        Posting posting = postingRepository.save(
                Posting.create(title, null, location, "신입", category, company));
        postingCount++;

        List<PostingStage> stages = addStages(posting, specs);
        autoRegister(posting, stages);
    }

    /**
     * 전형 저장. 발표 예정일이 TODAY 이전이면 발표된 것으로 보고 announcedDate 를 기록한다.
     * run() 에 @Transactional 이 없어 더티 체킹이 동작하지 않으므로 save() 이전에 세팅해야 한다.
     */
    private List<PostingStage> addStages(Posting posting, StageSpec... specs) {
        List<PostingStage> saved = new ArrayList<>();
        for (StageSpec s : specs) {
            PostingStage stage = PostingStage.create(
                    s.name(), s.orderIndex(), s.announceDate(), s.score(), posting);

            if (isAnnounced(s.announceDate())) {
                stage.markAnnouncedIfAbsent(s.announceDate());
                announcedCount++;
            }
            stageCount++;
            saved.add(postingStageRepository.save(stage));
        }
        return saved;
    }

    private boolean isAnnounced(LocalDate announceDate) {
        return announceDate != null && announceDate.isBefore(TODAY);
    }

    /**
     * 발표된 전형에만 등록을 생성한다.
     * 건수는 공고 제목 해시로 정해져 실행마다 동일하고, orderIndex 가 커질수록 줄어드는 깔때기 형태.
     * 어떤 경우에도 MIN_REGISTRATIONS 미만으로 내려가지 않는다.
     */
    private void autoRegister(Posting posting, List<PostingStage> stages) {
        int base = 18 + Math.floorMod(posting.getTitle().hashCode(), 15);   // 18 ~ 32

        for (PostingStage stage : stages) {
            if (stage.getAnnouncedDate() == null) {
                continue;   // 미발표 전형 → 등록 없음
            }

            int count = Math.max(MIN_REGISTRATIONS, base - (stage.getOrderIndex() - 1) * 6);
            int fail = count >= 10 ? count / 5 : 0;
            int pending = count >= 15 ? count / 6 : 0;
            int pass = count - fail - pending;

            registerResults(posting, stage, pass, RegistrationResult.PASS, 0);
            registerResults(posting, stage, fail, RegistrationResult.FAIL, pass);
            registerResults(posting, stage, pending, RegistrationResult.PENDING, pass + fail);
        }
    }

    private void registerResults(Posting posting, PostingStage stage,
                                 int count, RegistrationResult result, int offset) {
        if (count <= 0) {
            return;
        }
        if (offset + count > users.size()) {
            throw new IllegalArgumentException(
                    "offset+count(" + (offset + count) + ") > 유저수(" + users.size() + "). 공고=" + posting.getTitle());
        }
        for (int i = 0; i < count; i++) {
            registrationRepository.save(
                    Registration.create(users.get(offset + i), posting, stage, result,
                            List.of(ContactMethod.EMAIL), LocalDateTime.now(), false));
            registrationCount++;
        }
    }
}