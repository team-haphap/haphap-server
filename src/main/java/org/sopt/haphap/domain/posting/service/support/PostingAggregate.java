package org.sopt.haphap.domain.posting.service.support;

import java.util.Map;
import org.sopt.haphap.domain.posting.domain.Posting;

public record PostingAggregate(
        Map<Long, Posting> postingMap,
        Map<Long, String> companyImageByCompanyId
) {
    public Posting posting(Long postingId) {
        return postingMap.get(postingId);
    }

    public String companyImageUrl (Long postingId){
        Posting posting = posting(postingId);
        return posting == null ? null : companyImageByCompanyId.get(posting.getCompany().getId());
    }
}
