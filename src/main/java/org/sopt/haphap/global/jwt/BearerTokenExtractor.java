package org.sopt.haphap.global.jwt;
import org.springframework.util.StringUtils;

public class BearerTokenExtractor {

    private BearerTokenExtractor() {}

    public static String extract(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}