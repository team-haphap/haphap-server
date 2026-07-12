package org.sopt.haphap.domain.posting.service.support;

import java.util.List;
import java.util.Map;
import org.sopt.haphap.domain.posting.domain.Posting;
import org.sopt.haphap.domain.posting.dto.projection.PostingStageFlatProjection;

public record PostingAggregate(
        Map<Long, Posting> postingMap,
        Map<Long, List<PostingStageFlatProjection>> stagesByPosting,
        Map<Long, Map<Long, Long>> countsByPosting,   // 누적 전체 등록수 (nextStage 판정용)
        Map<Long, String> companyImageByCompanyId
) {
    public Posting posting(Long postingId) {
        return postingMap.get(postingId);
    }

    public List<PostingStageFlatProjection> stages(Long postingId) {
        return stagesByPosting.getOrDefault(postingId, List.of());
    }

    public Map<Long, Long> counts(Long postingId) {
        return countsByPosting.getOrDefault(postingId, Map.of());
    }

    public String companyImageUrl (Long postingId){
        Posting posting = posting(postingId);
        return posting == null ? null : companyImageByCompanyId.get(posting.getCompany().getId());
    }
}