package org.sopt.haphap.domain.content.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    REVIEW("후기"),
    ESSAY("자소서");

    private final String description;
}