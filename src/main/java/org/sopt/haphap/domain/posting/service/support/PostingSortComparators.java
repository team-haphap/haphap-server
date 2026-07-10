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
                .comparingInt((Scored s) -> rank(s.announceDate(), today))
                .thenComparing(Scored::announceDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Scored::title, korean);
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