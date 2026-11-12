package org.sopt.haphap.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.sopt.haphap.domain.search.dto.HighlightRange;
import org.sopt.haphap.domain.search.engine.SearchTextNormalizer;
import org.springframework.stereotype.Component;

@Component
public class HighlightRangeCalculator {

    /*
     * 표시 텍스트에서 입력값과 "직접 매칭"되는 구간을 찾는다
     * - 전체 입력이 통째로 있으면 그 구간 1개
     * - 없으면 단어별로 각각 찾아 여러 구간 반환 (ex. "카카오 인턴")
     * - 약칭/한영 동의어로만 매칭된 경우 직접 일치가 없으므로 빈 목록 (하이라이트 안 함)
     */

    public List<HighlightRange> calculate(String text, String keyword) {
        String lowerText = text.toLowerCase(Locale.KOREAN);

        List<HighlightRange> whole = find(lowerText, keyword.trim().toLowerCase(Locale.KOREAN));
        if (!whole.isEmpty()) {
            return merge(whole);
        }

        List<HighlightRange> ranges = new ArrayList<>();
        for (String token : SearchTextNormalizer.tokenize(keyword)) {
            ranges.addAll(find(lowerText, token));
        }
        return merge(ranges);
    }

    // text 안의 모든 등장 위치
    private List<HighlightRange> find(String lowerText, String lowerTerm) {
        List<HighlightRange> result = new ArrayList<>();
        if (lowerTerm.isEmpty()) {
            return result;
        }
        int from = 0;
        int idx;
        while ((idx = lowerText.indexOf(lowerTerm, from)) >= 0) {
            result.add(new HighlightRange(idx, idx + lowerTerm.length()));
            from = idx + 1;
        }
        return result;
    }

    // 시작 위치 정렬 후 겹치거나 맞닿은 구간 병합
    private List<HighlightRange> merge(List<HighlightRange> ranges) {
        List<HighlightRange> sorted = new ArrayList<>(ranges);
        sorted.sort((a, b) -> Integer.compare(a.start(), b.start()));
        List<HighlightRange> merged = new ArrayList<>();
        for (HighlightRange r : sorted) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).end() >= r.start()) {
                HighlightRange last = merged.remove(merged.size() - 1);
                merged.add(new HighlightRange(last.start(), Math.max(last.end(), r.end())));
            } else {
                merged.add(r);
            }
        }
        return merged;
    }
}