package org.sopt.haphap.domain.passcard.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.passcard.code.PassCardSuccessCode;
import org.sopt.haphap.domain.passcard.dto.response.PassCardArchiveListResponse;
import org.sopt.haphap.domain.passcard.dto.response.UnconfirmedPassCardListResponse;
import org.sopt.haphap.domain.passcard.service.PassCardService;
import org.sopt.haphap.domain.registration.dto.response.PassCardResponse;
import org.sopt.haphap.global.dto.ApiResponse;
import org.sopt.haphap.global.dto.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pass-cards")
@RequiredArgsConstructor
public class PassCardController implements PassCardApiDocs {

    private final PassCardService passCardService;

    @GetMapping("/unconfirmed")
    public ResponseEntity<SuccessResponse<UnconfirmedPassCardListResponse>> getUnconfirmedPassCards(
            @AuthenticationPrincipal Long userId) {
        SuccessResponse<UnconfirmedPassCardListResponse> body = ApiResponse.success(
                PassCardSuccessCode.PASS_CARD_UNCONFIRMED_FETCHED,
                passCardService.getUnconfirmedPassCards(userId));
        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PassCardArchiveListResponse>> getArchive(
            @AuthenticationPrincipal Long userId) {
        SuccessResponse<PassCardArchiveListResponse> body = ApiResponse.success(
                PassCardSuccessCode.PASS_CARD_LIST_FETCHED,
                passCardService.getArchive(userId));
        return ResponseEntity.status(body.status()).body(body);
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<SuccessResponse<PassCardResponse>> getPassCard(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long registrationId) {
        SuccessResponse<PassCardResponse> body = ApiResponse.success(
                PassCardSuccessCode.PASS_CARD_FETCHED,
                passCardService.getPassCard(userId, registrationId));
        return ResponseEntity.status(body.status()).body(body);
    }

    @PostMapping("/{registrationId}/confirm")
    public ResponseEntity<Void> confirm(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long registrationId) {
        passCardService.confirm(userId, registrationId);
        return ResponseEntity.noContent().build();
    }
}