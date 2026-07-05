package org.sopt.haphap.domain.search.service;

import java.util.List;
import java.util.Locale;
import org.sopt.haphap.domain.search.dto.HighlightRange;
import org.springframework.stereotype.Component;

@Component
public class HighlightRangeCalculator {

    //text 안에서 keyword가 처음 등장하는 위치를 찾아 하이라이트 범위로 반환하는 아이입니다

    public List<HighlightRange> calculate(String text, String keyword) {
        String lowerText = text.toLowerCase(Locale.KOREAN);
        String lowerKeyword = keyword.toLowerCase(Locale.KOREAN);

        int start = lowerText.indexOf(lowerKeyword);
        if (start < 0) {
            return List.of();
        }
        return List.of(new HighlightRange(start, start + keyword.length()));
    }
}