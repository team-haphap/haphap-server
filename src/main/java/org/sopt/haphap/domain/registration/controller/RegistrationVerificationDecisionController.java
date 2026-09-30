package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.security.VerificationDecisionToken;
import org.sopt.haphap.domain.registration.security.VerificationDecisionType;
import org.sopt.haphap.domain.registration.security.VerificationLinkSigner;
import org.sopt.haphap.domain.registration.service.RegistrationVerificationDecisionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/registrations/verification")
@RequiredArgsConstructor
@Hidden
public class RegistrationVerificationDecisionController {

    private final VerificationLinkSigner linkSigner;
    private final RegistrationVerificationDecisionService decisionService;

    // 디스코드 링크 클릭 시 도착하는 곳. 상태 변경 절대 금지!
    // 디스코드/슬랙 등은 링크 미리보기를 만들려고 이 URL에 자동으로 GET을 미리 보내기도 해서,
    // 여기서 승인/반려를 실행해버리면 클릭도 안 했는데 처리되는 사고가 남.

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String showConfirmPage(@RequestParam String token) {
        VerificationDecisionToken decoded = linkSigner.verify(token); // 위변조/만료면 여기서 예외

        String actionLabel = decoded.action() == VerificationDecisionType.APPROVE ? "승인" : "반려";
        return """
                <html><body style="font-family:sans-serif;text-align:center;padding-top:80px;">
                    <h2>등록 #%d 건을 %s 처리할까요?</h2>
                    <form method="post" action="/admin/registrations/verification/decide">
                        <input type="hidden" name="token" value="%s"/>
                        <button type="submit" style="padding:12px 24px;font-size:16px;">%s 확정</button>
                    </form>
                </body></html>
                """.formatted(decoded.registrationId(), actionLabel, token, actionLabel);
    }

    // 확인 페이지에서 버튼 눌렀을 때만 실제 처리
    @PostMapping("/decide")
    public ResponseEntity<String> decide(@RequestParam String token) {
        VerificationDecisionToken decoded = linkSigner.verify(token);

        if (decoded.action() == VerificationDecisionType.APPROVE) {
            decisionService.approve(decoded.registrationId());
        } else {
            decisionService.reject(decoded.registrationId());
        }
        return ResponseEntity.ok("처리 완료");
    }
}