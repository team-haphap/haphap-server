package org.sopt.haphap.domain.registration.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.registration.code.RegistrationErrorCode;
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
    private static final int MAX_REASON_LENGTH = 500;
    private static final String DEFAULT_REJECT_REASON = "합격을 인증할 수 있는 화면을 다시 제출해주세요.";

    private final VerificationLinkSigner linkSigner;
    private final RegistrationVerificationDecisionService decisionService;

    @GetMapping
    public ResponseEntity<String> showConfirmPage(@RequestParam String token) {
        VerificationDecisionToken decoded = linkSigner.verify(token);

        String actionLabel = decoded.action() == VerificationDecisionType.APPROVE ? "승인" : "반려";

        String body;
        if (decoded.action() == VerificationDecisionType.REJECT) {
            // 반려: 사유 입력 textarea 포함
            body = """
                <h2>등록 #%d 건을 반려 처리할까요?</h2>
                <form method="post" action="/admin/registrations/verification/decide">
                    <input type="hidden" name="token" value="%s"/>
                    <div style="margin-bottom:16px;">
                        <label for="reason" style="display:block;margin-bottom:8px;font-weight:bold;">반려 사유</label>
                        <textarea id="reason" name="reason" rows="4" cols="50"
                                  maxlength="%d"
                                  style="width:100%%;max-width:500px;padding:8px;font-size:14px;"
                        >%s</textarea>
                        <div style="color:#666;font-size:12px;margin-top:4px;">최대 %d자</div>
                    </div>
                    <button type="submit" style="padding:12px 24px;font-size:16px;background:#e74c3c;color:#fff;border:none;border-radius:6px;cursor:pointer;">반려 확정</button>
                </form>
                """.formatted(
                    decoded.registrationId(),
                    HtmlUtils.htmlEscape(token),
                    MAX_REASON_LENGTH,
                    HtmlUtils.htmlEscape(DEFAULT_REJECT_REASON),
                    MAX_REASON_LENGTH
            );
        } else {
            // 승인: 기존과 동일
            body = """
                <h2>등록 #%d 건을 승인 처리할까요?</h2>
                <form method="post" action="/admin/registrations/verification/decide">
                    <input type="hidden" name="token" value="%s"/>
                    <button type="submit" style="padding:12px 24px;font-size:16px;background:#2ecc71;color:#fff;border:none;border-radius:6px;cursor:pointer;">승인 확정</button>
                </form>
                """.formatted(decoded.registrationId(), HtmlUtils.htmlEscape(token));
        }

        return html(ResponseEntity.ok(), body);
    }

    @PostMapping("/decide")
    public ResponseEntity<String> decide(
            @RequestParam String token,
            @RequestParam(required = false) String reason
    ) {
        VerificationDecisionToken decoded = linkSigner.verify(token);

        String actionLabel;
        if (decoded.action() == VerificationDecisionType.APPROVE) {
            decisionService.approve(decoded.registrationId(), decoded.round());
            actionLabel = "승인";
        } else {
            // 반려 사유 검증
            String trimmedReason = (reason != null) ? reason.trim() : "";
            if (trimmedReason.isEmpty() || trimmedReason.length() > MAX_REASON_LENGTH) {
                throw new CustomException(RegistrationErrorCode.INVALID_REJECT_REASON);
            }
            decisionService.reject(decoded.registrationId(), decoded.round(), trimmedReason);
            actionLabel = "반려";
        }
        return html(ResponseEntity.ok(),
                "<h2>등록 #%d 건이 %s 처리되었어요.</h2>".formatted(decoded.registrationId(), actionLabel));
    }

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