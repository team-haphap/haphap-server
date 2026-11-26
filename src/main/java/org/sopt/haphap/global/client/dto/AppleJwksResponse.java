package org.sopt.haphap.global.client.dto;

import java.util.List;

public record AppleJwksResponse(List<AppleJwk> keys) {
    public record AppleJwk(String kty, String kid, String use, String alg, String n, String e) {}
}