package org.sopt.haphap.domain.posting.service.support;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import org.sopt.haphap.domain.posting.service.support.PostingResponseAssembler.Scored;

public final class PostingSortComparators {

    private PostingSortComparators() {}

    /** 발표일 가까운 순 → 과거 → null 순으로 뒤. 동일 발표일이면 공고명 가나다순. */
    public static Comparator<Scored> byAnnounceDate() {
        Collator korean = Collator.getInstance(Locale.KOREAN);
        LocalDate today = LocalDate.now();
        return Comparator
                // ① 마감이 아닌 것(예정)이 먼저, 마감이 뒤로
                .comparingInt((Scored s) -> isClosed(s) ? 1 : 0)
                // ② 그룹 내 정렬 — 예정은 발표일 오름차순, 마감은 마감일 내림차순
                .thenComparing((a, b) -> {
                    if (isClosed(a)) {
                        // 마감 그룹: 마감일 내림차순 (null은 맨 뒤)
                        return Comparator
                                .comparing(Scored::deadline,
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .compare(a, b);
                    }
                    // 예정 그룹: 발표일 오름차순
                    return Comparator
                            .comparing(Scored::announceDate,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .compare(a, b);
                })
                // ③ 동점: 가나다순
                .thenComparing(Scored::title, korean);
    }
    // 발표일(nextStage)이 없으면 마감으로 간주
    private static boolean isClosed(Scored s) {
        return s.announceDate() == null;
    }

    public static Comparator<Scored> byDeadline() {
        Collator korean = Collator.getInstance(Locale.KOREAN);
        return Comparator
                .comparing(Scored::deadline, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Scored::title, korean);
    }

    private static int rank(LocalDate date, LocalDate today) {
        if (date == null) return 2;
        return date.isBefore(today) ? 1 : 0;
    }
}