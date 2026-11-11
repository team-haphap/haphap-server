package org.sopt.haphap;

import org.junit.jupiter.api.Test;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class TempTokenTest {

    @Autowired JwtProvider jwtProvider;

    @Test
    void print() {
        System.out.println("ME_ACCESS=" + jwtProvider.createAccessToken(33L));
        System.out.println("OTHER_ACCESS=" + jwtProvider.createAccessToken(34L));
    }
}