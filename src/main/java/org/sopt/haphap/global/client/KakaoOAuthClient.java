package org.sopt.haphap.global.client;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.global.client.dto.KakaoUserResponse;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final WebClient webClient;

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        KakaoUserResponse response = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        r -> Mono.error(new CustomException(AuthErrorCode.KAKAO_INVALID_TOKEN)))
                .onStatus(HttpStatusCode::isError,
                        r -> Mono.error(new CustomException(AuthErrorCode.KAKAO_INVALID_TOKEN)))
                .bodyToMono(KakaoUserResponse.class)
                .switchIfEmpty(Mono.error(new CustomException(AuthErrorCode.KAKAO_ACCOUNT_NOT_FOUND)))
                .onErrorMap(ex -> !(ex instanceof CustomException),
                        ex -> new CustomException(AuthErrorCode.KAKAO_INVALID_TOKEN))
                .block();

        if (response == null) {
            throw new CustomException(AuthErrorCode.KAKAO_ACCOUNT_NOT_FOUND);
        }
        KakaoUserResponse.KakaoAccount account = response.kakaoAccount();
        if (account == null) {
            throw new CustomException(AuthErrorCode.KAKAO_ACCOUNT_NOT_FOUND);
        }

        return new OAuthUserInfo(
                String.valueOf(response.id()),
                account.name(),
                account.email(),
                parseBirthDate(
                        account.birthyear() != null ? account.birthyear() : "",
                        account.birthday() != null ? account.birthday() : ""
                )
        );
    }

    private LocalDate parseBirthDate(String birthyear, String birthday) {
        try {
            return LocalDate.parse(birthyear + birthday, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }
}