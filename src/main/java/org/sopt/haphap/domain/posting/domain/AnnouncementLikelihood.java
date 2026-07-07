package org.sopt.haphap.domain.posting.domain;

public enum AnnouncementLikelihood {
    NONE,
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH;

    // TODO: 정확한 구간은 기획 확정 후 조정할게요
    private static final int VERY_HIGH_THRESHOLD = 80;
    private static final int HIGH_THRESHOLD = 60;
    private static final int MEDIUM_THRESHOLD = 40;
    private static final int LOW_THRESHOLD = 20;

    public static AnnouncementLikelihood from(int expectedScore) {
        if (expectedScore >= VERY_HIGH_THRESHOLD) {
            return VERY_HIGH;
        }
        if (expectedScore >= HIGH_THRESHOLD) {
            return HIGH;
        }
        if (expectedScore >= MEDIUM_THRESHOLD) {
            return MEDIUM;
        }
        if (expectedScore >= LOW_THRESHOLD) {
            return LOW;
        }
        return VERY_LOW;
    }
}