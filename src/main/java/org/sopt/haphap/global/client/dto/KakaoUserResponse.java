package org.sopt.haphap.global.client.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String name,
            String email,
            String birthyear,
            String birthday,
            String gender,
            @JsonProperty("age_range") String ageRange,
            @JsonProperty("phone_number") String phoneNumber,
            Profile profile
    ) {}
    public record Profile(
            String nickname
    ) {}
}