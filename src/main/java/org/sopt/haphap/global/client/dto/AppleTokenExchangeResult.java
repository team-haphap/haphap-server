package org.sopt.haphap.global.client.dto;

//애플 인가 코드 교환 결과: 연동 해제용 refresh token + 사용자 검증용 id token

public record AppleTokenExchangeResult(String refreshToken, String idToken) {}