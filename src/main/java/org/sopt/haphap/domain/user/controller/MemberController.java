package org.sopt.haphap.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.dto.MemberResponse;
import org.sopt.haphap.domain.user.service.MemberService;
import org.sopt.haphap.global.code.MemberSuccessCode;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.sopt.haphap.global.jwt.BearerTokenExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberApiDocs {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MemberResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MEMBER_INFO_FETCHED, memberService.getMyInfo(userId)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long userId,
            @RequestHeader("Authorization") String authorization) {
        String token = BearerTokenExtractor.extract(authorization);
        memberService.withdraw(userId, token);
        return ResponseEntity.noContent().build();
    }
}