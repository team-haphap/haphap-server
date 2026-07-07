package org.sopt.haphap.domain.posting.domain;

public enum AnnouncementLikelihood {
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    // TODO: 정확한 구간은 기획 확정 후 조정할게요
    private static final int HIGH_THRESHOLD = 70;
    private static final int MEDIUM_THRESHOLD = 40;

    public static AnnouncementLikelihood from(int expectedScore) {
        if (expectedScore >= HIGH_THRESHOLD) {
            return HIGH;
        }
        if (expectedScore >= MEDIUM_THRESHOLD) {
            return MEDIUM;
        }
        return LOW;
    }
}