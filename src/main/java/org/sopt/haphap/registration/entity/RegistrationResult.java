package org.sopt.haphap.registration.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationResult {
    PASS("합격"),
    FAIL("불합격"),
    PENDING("대기");

    private final String description;
}