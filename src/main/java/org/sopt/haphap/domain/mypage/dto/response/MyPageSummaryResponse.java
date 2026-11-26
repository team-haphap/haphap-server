package org.sopt.haphap.domain.mypage.dto.response;

import org.sopt.haphap.domain.registration.projection.PassCertificationCountProjection;
import org.sopt.haphap.domain.user.entity.User;

public record MyPageSummaryResponse(
        Member member,
        PassCertification passCertification,
        boolean hasUpcomingAnnouncement   // 이번 주(월~일) 발표 예상 전형이 있는 공고가 하나라도 있는지 (전체 공고 기준)
) {

    public record Member(
            Long memberId,
            String nickname,
            String profileImageUrl,
            int point
    ) {
        public static Member from(User user) {
            return new Member(user.getId(), user.getAnonymousName(), user.getProfileImageUrl(), user.getPointBalance());
        }
    }

    // 내 합격 등록의 인증 결과. 승인 대기 중인 건은 어느 쪽에도 포함되지 않는다.
    public record PassCertification(
            long rejectedCount,
            long approvedCount
    ) {
        public static PassCertification from(PassCertificationCountProjection counts) {
            return new PassCertification(counts.getRejectedCount(), counts.getApprovedCount());
        }
    }

    public static MyPageSummaryResponse of(User user, PassCertificationCountProjection counts,
                                           boolean hasUpcomingAnnouncement) {
        return new MyPageSummaryResponse(Member.from(user), PassCertification.from(counts), hasUpcomingAnnouncement);
    }
}
