package org.sopt.haphap.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.MemberProfileResponse;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.domain.user.dto.WithdrawRequest;
import org.sopt.haphap.domain.user.service.MemberProfileService;
import org.sopt.haphap.domain.user.service.MemberService;
import org.sopt.haphap.domain.user.service.MemberWithdrawalService;
import org.sopt.haphap.global.code.MemberSuccessCode;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.jwt.BearerTokenExtractor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberApiDocs {

    private final MemberService memberService;
    private final MemberWithdrawalService memberWithdrawalService;
    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MemberResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_INFO_FETCHED, memberService.getMyInfo(userId)));
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<MemberProfileResponse>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String nickname,
            @RequestPart(required = false) MultipartFile profileImage) {
        MemberProfileResponse response = memberProfileService.updateProfile(userId, nickname, profileImage);
        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_PROFILE_UPDATED, response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody WithdrawRequest request) {
        String token = BearerTokenExtractor.extract(authorization);
        memberWithdrawalService.withdraw(userId, token, request);
        return ResponseEntity.noContent().build();
    }
}