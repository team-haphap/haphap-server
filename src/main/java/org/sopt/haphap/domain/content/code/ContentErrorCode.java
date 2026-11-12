import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.code.ErrorResultCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContentErrorCode implements ErrorResultCode {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 콘텐츠입니다.");

    private final HttpStatus status;
    private final String message;
}