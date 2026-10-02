package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.security.VerificationDecisionToken;
import org.sopt.haphap.domain.registration.security.VerificationDecisionType;
import org.sopt.haphap.domain.registration.security.VerificationLinkSigner;
import org.sopt.haphap.domain.registration.service.RegistrationVerificationDecisionService;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/admin/registrations/verification")
@RequiredArgsConstructor
@Hidden
public class RegistrationVerificationDecisionController {

    private static final MediaType HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

    private final VerificationLinkSigner linkSigner;
    private final RegistrationVerificationDecisionService decisionService;

    // 디스코드 링크 클릭 시 도착하는 곳. 상태 변경 절대 금지!
    // 디스코드/슬랙 등은 링크 미리보기를 만들려고 이 URL에 자동으로 GET을 미리 보내기도 해서,
    // 여기서 승인/반려를 실행해버리면 클릭도 안 했는데 처리되는 사고가 남.
    @GetMapping
    public ResponseEntity<String> showConfirmPage(@RequestParam String token) {
        VerificationDecisionToken decoded = linkSigner.verify(token); // 위변조면 여기서 예외

        String actionLabel = decoded.action() == VerificationDecisionType.APPROVE ? "승인" : "반려";
        String body = """
                <h2>등록 #%d 건을 %s 처리할까요?</h2>
                <form method="post" action="/admin/registrations/verification/decide">
                    <input type="hidden" name="token" value="%s"/>
                    <button type="submit" style="padding:12px 24px;font-size:16px;">%s 확정</button>
                </form>
                """.formatted(decoded.registrationId(), actionLabel, HtmlUtils.htmlEscape(token), actionLabel);

        return html(ResponseEntity.ok(), body);
    }

    // 확인 페이지에서 버튼 눌렀을 때만 실제 처리
    @PostMapping("/decide")
    public ResponseEntity<String> decide(@RequestParam String token) {
        VerificationDecisionToken decoded = linkSigner.verify(token);

        String actionLabel;
        if (decoded.action() == VerificationDecisionType.APPROVE) {
            decisionService.approve(decoded.registrationId());
            actionLabel = "승인";
        } else {
            decisionService.reject(decoded.registrationId());
            actionLabel = "반려";
        }
        return html(ResponseEntity.ok(),
                "<h2>등록 #%d 건이 %s 처리되었어요.</h2>".formatted(decoded.registrationId(), actionLabel));
    }

    // 운영진이 브라우저로 보는 페이지라, 전역 핸들러의 JSON/XML 대신 HTML로 오류를 보여준다.
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleError(CustomException e) {
        ErrorResultCode code = e.getErrorCode();
        return html(ResponseEntity.status(code.getStatus()),
                "<h2>%s</h2>".formatted(HtmlUtils.htmlEscape(code.getMessage())));
    }

    private ResponseEntity<String> html(ResponseEntity.BodyBuilder builder, String body) {
        return builder.contentType(HTML_UTF8).body(page(body));
    }

    private String page(String body) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>합격 인증 처리</title></head>
                <body style="font-family:sans-serif;text-align:center;padding-top:80px;">
                %s
                </body>
                </html>
                """.formatted(body);
    }
}