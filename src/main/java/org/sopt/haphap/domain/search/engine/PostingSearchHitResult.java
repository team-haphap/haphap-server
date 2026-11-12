package org.sopt.haphap.domain.search.engine;

import java.util.List;

/* all: 모든 단어를 충족, partial: 일부 단어만 충족(후순위) */
public record PostingSearchHitResult(List<PostingSearchHit> all, List<PostingSearchHit> partial) {

    public static PostingSearchHitResult empty() {
        return new PostingSearchHitResult(List.of(), List.of());
    }
}