package org.sopt.haphap.global.util;

import org.hibernate.exception.ConstraintViolationException;

// DataIntegrityViolationException이 어떤 제약 때문에 났는지 구분 (특정 유니크 위반만 비즈니스 에러로 바꾸고, 나머지는 그대로 던지기 위해)
public final class ConstraintViolations {

    private ConstraintViolations() {}

    public static boolean isViolationOf(Throwable e, String constraintName) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && constraintName.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
