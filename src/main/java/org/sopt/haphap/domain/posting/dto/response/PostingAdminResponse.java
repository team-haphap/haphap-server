package org.sopt.haphap.domain.posting.dto.response;

import org.sopt.haphap.domain.posting.domain.Posting;

import java.time.LocalDate;

public record PostingAdminResponse(
        Long id, String title, LocalDate deadline, String location, String position,
        Long categoryId, String categoryName, Long companyId, String companyName
) {
    public static PostingAdminResponse from(Posting posting) {
        return new PostingAdminResponse(
                posting.getId(), posting.getTitle(), posting.getDeadline(),
                posting.getLocation(), posting.getPosition(),
                posting.getCategory().getId(), posting.getCategory().getName(),
                posting.getCompany().getId(), posting.getCompany().getName());
    }
}

