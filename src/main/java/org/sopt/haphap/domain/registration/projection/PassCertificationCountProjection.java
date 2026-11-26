package org.sopt.haphap.domain.registration.projection;

public interface PassCertificationCountProjection {
    Long getApprovedCount();   // 합격 인증 승인 건수
    Long getRejectedCount();   // 합격 인증 반려 건수
}
