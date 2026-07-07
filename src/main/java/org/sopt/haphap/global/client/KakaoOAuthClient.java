package org.sopt.haphap.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.user.entity.Provider;
import org.sopt.haphap.global.client.dto.KakaoUserResponse;
import org.sopt.haphap.global.client.dto.OAuthUserInfo;
import org.sopt.haphap.global.code.AuthErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.sopt.haphap.global.util.PhoneNumberMasker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final WebClient webClient;
    private final PhoneNumberMasker phoneNumberMasker;

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
                .onStatus(HttpStatusCode::is5xxServerError,
                        r -> Mono.error(new CustomException(AuthErrorCode.KAKAO_SERVER_UNAVAILABLE)))
                .onStatus(status -> status.is4xxClientError() && status.value() != 401,
                        r -> Mono.error(new CustomException(AuthErrorCode.KAKAO_INVALID_TOKEN)))
                .bodyToMono(KakaoUserResponse.class)
                .switchIfEmpty(Mono.error(new CustomException(AuthErrorCode.KAKAO_ACCOUNT_NOT_FOUND)))
                .onErrorMap(ex -> !(ex instanceof CustomException),
                        ex -> {
                            log.error("Kakao API 호출 실패", ex);
                            return new CustomException(AuthErrorCode.KAKAO_SERVER_UNAVAILABLE);
                        })
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
                ),
                account.gender(),
                account.ageRange(),
                phoneNumberMasker.mask(account.phoneNumber())
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