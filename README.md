# HAPHAP-SERVER

---
**합합**은 채용 공고 지원 이후 발생하는 정보 공백을 줄이기 위한 채용 결과 공유 플랫폼입니다.
<br/><br/>
본 저장소는 합합 서비스의 백엔드 서버로, 공고·전형 단계·지원 결과 데이터를 관리하고 카카오 로그인, 푸시 알림, 검색, 캘린더 등 클라이언트가 필요로 하는 API를 제공합니다.
<br/>
카카오 소셜 로그인 기반 인증부터 전형 결과 등록, 공고별 통계 집계, FCM 푸시 알림, 무중단 배포까지 서비스 운영에 필요한 백엔드 기능 전반을 다룹니다.
<br/>


## **✨ Contributors**

| <a href="https://github.com/byunhm02"><img width="117" height="116" alt="변희민 프로필" src="https://github.com/user-attachments/assets/29e1d541-ba30-4033-bd28-b5ddff6a39fc" /></a> | <a href="https://github.com/sophie-24"><img width="95" height="119" alt="김규리 프로필" src="https://github.com/user-attachments/assets/b91a45b7-ae11-4a59-9702-9a0775d31c59" /></a> |
| :---: | :---: |
| **변희민** <br> (LEAD) <br><br> [@byunhm02](https://github.com/byunhm02) | **김규리** <br> (MEMBER) <br><br> [@sophie-24](https://github.com/sophie-24) |


<br/>

## **⚒️ Tech Stack**

| 항목 | 기술 스택 |
| :--- | :--- |
| Language / Runtime | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Build Tool | Gradle |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| Cache | Redis 7 |
| Auth | Spring Security, JWT (jjwt), Kakao OAuth |
| Push Notification | Firebase Admin SDK (FCM) |
| Storage | AWS S3 (webp 이미지 변환) |
| API Docs | springdoc-openapi (Swagger UI) |
| Resilience | Spring Retry, Spring AOP |
| HTTP Client | Spring WebFlux (WebClient) |
| Monitoring | Spring Actuator (헬스체크), Dozzle (컨테이너 로그 뷰어) |
| Infra / CI-CD | Docker, Docker Compose, Nginx, GitHub Actions |

<br/>

## **✨ 주요 기능**

| 도메인 | 기능 |
| :--- | :--- |
| `user` | 카카오 소셜 로그인, JWT 발급 · 재발급 · 로그아웃, 회원 정보 조회 |
| `posting` | 공고 목록 · 상세 · 오늘의 공고 조회, 전형 단계 조회, 조회수 · 카드 클릭수 집계, 전형 단계별 · 오늘의 통계, 관리자용 공고 · 회사 · 카테고리 · 전형 단계 등록 |
| `registration` | 지원 등록, 공고별 전형 결과(합격/불합격/대기 등) 등록 |
| `alram` | 알림 on/off 설정, FCM 푸시 토큰 등록, 상태 변경 시 푸시 알림 발송 |
| `calendar` | 날짜별 공고 캘린더 조회, 월별 지표(인디케이터) 조회 |
| `search` | 인기 검색어, 검색어 자동완성, 검색 결과 공고 목록 조회 |
| `banner` | 홈 배너 목록 조회 |
| `admin` | 관리자 로그인 및 인증 |
| `global` | 이미지 업로드(S3, webp 변환, 다중 업로드), 공통 응답 · 에러 코드, 전역 예외 처리, JWT 인증 필터 |

<br/>

## **🚀 배포 & 운영**

- **CI/CD**: GitHub Actions로 빌드 → Docker 이미지 빌드/푸시 → EC2 배포까지 자동화
- **무중단 배포**: Docker blue-green 방식 + Nginx 포트 스위칭, `/actuator/health` 헬스체크 통과 후 트래픽 전환
- **로그 모니터링**: Dozzle로 컨테이너 로그 실시간 확인
- **DB 마이그레이션**: Flyway로 스키마 버전 관리

<br/>

## **🗂️ Project Structure**

```text
🗃️ haphap-server
└── 📂 src/main/java/org/sopt/haphap
    ├── 📂 domain
    │   ├── 📁 user            # 카카오 로그인, JWT 인증, 회원 정보
    │   ├── 📁 posting         # 공고, 회사, 카테고리, 전형 단계, 통계
    │   ├── 📁 registration    # 지원 등록, 전형 결과
    │   ├── 📁 alram           # 알림 설정, FCM 푸시
    │   ├── 📁 calendar        # 캘린더 지표
    │   ├── 📁 search          # 검색, 자동완성, 인기 검색어
    │   ├── 📁 banner          # 홈 배너
    │   └── 📁 admin           # 관리자 인증
    │
    └── 📂 global
        ├── 📁 client           # 카카오 OAuth 등 외부 API 클라이언트
        ├── 📁 code             # 성공/에러 코드 정의
        ├── 📁 config           # JPA, Swagger, WebClient, Retry 설정
        ├── 📁 controller       # 공통 이미지 업로드 컨트롤러
        ├── 📁 dto              # 공통 응답 DTO
        ├── 📁 exception        # 전역 예외 처리
        ├── 📁 filter           # 요청 필터
        ├── 📁 jwt              # JWT 발급/검증, 인증 필터
        ├── 📁 s3               # S3 업로드 (webp 변환)
        └── 📁 util             # 공통 유틸
```
