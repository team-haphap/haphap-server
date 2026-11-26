package org.sopt.haphap.domain.verification.dto.response;

import org.sopt.haphap.domain.verification.entity.VerificationImage;

import java.util.List;

public record VerificationImageUploadResponse(List<Long> imageIds) {
    public static VerificationImageUploadResponse from(List<VerificationImage> images) {
        return new VerificationImageUploadResponse(images.stream().map(VerificationImage::getId).toList());
    }
}