package org.sopt.haphap.domain.verification.code;

import java.util.Set;

public final class VerificationImagePolicy {
    public static final int MIN_IMAGE_COUNT = 1;
    public static final int MAX_IMAGE_COUNT = 3;
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;   // 10MB
    public static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "png"); // JPG와 JPEG는 같은 포맷

    private VerificationImagePolicy() {}
}