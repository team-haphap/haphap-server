package org.sopt.haphap.global.client;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.global.client.dto.KakaoUserResponse;
import org.sopt.haphap.global.code.GlobalErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {

    private final WebClient webClient;

    public KakaoUserResponse getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                        status -> status.value() == 401,
                        response -> Mono.error(new CustomException(GlobalErrorCode.KAKAO_UNAUTHORIZED))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR))
                )
                .bodyToMono(KakaoUserResponse.class)
                .switchIfEmpty(Mono.error(new CustomException(GlobalErrorCode.BAD_REQUEST)))
                .onErrorMap(
                        ex -> !(ex instanceof CustomException),
                        ex -> new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR)
                )
                .block();
    }
}