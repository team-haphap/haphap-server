package org.sopt.haphap.global.util;

import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// 초성×중성 조합(399×399=159,201가지) 기반 랜덤이라 유저 수 증가 시 닉네임 중복 가능.

@Component
public class AnonymousNameGenerator {

    // 초성 19개
    private static final int CHOSUNG_COUNT = 19;
    // 중성 21개
    private static final int JUNGSUNG_COUNT = 21;
    // 종성 28개 (0 = 받침 없음) — 받침 포함하려면 generateSyllable()에서 random.nextInt(28) 로 변경
    private static final int JONGSUNG_NONE = 0;

    public String generate() {
        return "익명의 " + generateSyllable() + generateSyllable();
    }

    private char generateSyllable() {
        int chosung = ThreadLocalRandom.current().nextInt(CHOSUNG_COUNT);
        int jungsung = ThreadLocalRandom.current().nextInt(JUNGSUNG_COUNT);
        return (char) (0xAC00 + (chosung * 21 + jungsung) * 28 + JONGSUNG_NONE);
    }
}