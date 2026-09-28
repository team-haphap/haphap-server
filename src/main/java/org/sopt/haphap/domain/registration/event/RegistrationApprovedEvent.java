package org.sopt.haphap.domain.registration.event;

// 합격 인증 승인 완료 시 발행. 실제 승인 트리거(인증샷 검토 등)는 별도 파트에서 Registration.approve()를
// 호출하는 지점에 맞춰 이 이벤트를 발행하면 된다.
public record RegistrationApprovedEvent(Long postingId, Long stageId) {}
