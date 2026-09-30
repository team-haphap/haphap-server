package org.sopt.haphap.domain.verification.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.verification.code.VerificationSuccessCode;
import org.sopt.haphap.domain.verification.dto.response.VerificationImageUploadResponse;
import org.sopt.haphap.domain.verification.service.VerificationImageService;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registrations/verification-images")
@RequiredArgsConstructor
public class VerificationImageController implements VerificationImageApiDocs {

    private final VerificationImageService verificationImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<VerificationImageUploadResponse>> upload(
            @AuthenticationPrincipal Long userId,
            @RequestPart("files") List<MultipartFile> files) {
        VerificationImageUploadResponse response = verificationImageService.upload(userId, files);
        SuccessResponse<VerificationImageUploadResponse> body =
                ApiResponse.success(VerificationSuccessCode.VERIFICATION_IMAGE_UPLOADED, response);
        return ResponseEntity.status(body.status()).body(body);
    }
}
