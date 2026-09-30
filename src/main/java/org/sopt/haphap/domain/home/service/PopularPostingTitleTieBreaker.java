package org.sopt.haphap.domain.home.service;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * "지금 많이 보는 공고" 조회수 동점 처리 규칙: 공고명 가나다순, 우선순위는 숫자 → 한글 → 영문 → 그 외.
 * HomePopularPostingRefresher(캐시에 넣을 top-N을 정할 때)와 HomePopularPostingService(최종 응답을
 * 정렬할 때) 둘 다 반드시 같은 규칙을 써야 한다. 캐시를 만드는 쪽이 점수로만 자르고 이 규칙을 안 쓰면,
 * 동점자가 TOP_N_PER_KEY보다 많을 때 어떤 항목이 캐시에 남을지가 이 규칙과 무관하게(HashMap 순회 순서 등)
 * 정해져 버려서, 서비스가 아무리 이 규칙대로 정렬해도 애초에 캐시에 없는 항목은 결과에 나올 수 없다.
 */
public final class PopularPostingTitleTieBreaker {

    public static final Comparator<String> COMPARATOR = PopularPostingTitleTieBreaker::compare;

    private static final Collator TITLE_COLLATOR = Collator.getInstance(Locale.KOREAN);

    private PopularPostingTitleTieBreaker() {
    }

    public static int compare(String a, String b) {
        TitleGroup groupA = TitleGroup.of(firstChar(a));
        TitleGroup groupB = TitleGroup.of(firstChar(b));
        if (groupA != groupB) {
            return Integer.compare(groupA.ordinal(), groupB.ordinal());
        }
        return TITLE_COLLATOR.compare(a, b);
    }

    private static char firstChar(String value) {
        return (value == null || value.isEmpty()) ? Character.MAX_VALUE : value.charAt(0);
    }

    private enum TitleGroup {
        DIGIT, HANGUL, LATIN, OTHER;

        static TitleGroup of(char c) {
            if (Character.isDigit(c)) {
                return DIGIT;
            }
            if (isHangul(c)) {
                return HANGUL;
            }
            if (isLatin(c)) {
                return LATIN;
            }
            return OTHER;
        }

        private static boolean isHangul(char c) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || block == Character.UnicodeBlock.HANGUL_JAMO
                    || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                    || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
        }

        private static boolean isLatin(char c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }
    }
}
