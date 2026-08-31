package org.sopt.haphap.global.client;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class AppleClientSecretGenerator {

    @Value("${apple.team-id:}")
    private String teamId;

    @Value("${apple.key-id:}")
    private String keyId;

    @Value("${apple.client-id:}")
    private String clientId;

    @Value("${apple.private-key-path:}")
    private String privateKeyPath;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        if (privateKeyPath == null || privateKeyPath.isBlank()) {
            log.warn("apple.private-key-path가 설정되지 않아, 애플 연동 해제(revoke) 기능은 비활성 상태입니다.");
            return;
        }
        try {
            this.privateKey = loadPrivateKey(privateKeyPath);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "apple.private-key-path가 설정되어 있는데 Apple private key 로드에 실패했습니다 - "
                            + "설정이 잘못된 환경에서는 서버가 기동되면 안 됩니다.", e);
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    /* 호출 시점마다 5분짜리 client_secret을 새로 생성*/
    public String generate() {
        if (privateKey == null) {
            throw new IllegalStateException("애플 Private Key가 로드되지 않았습니다.");
        }
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000L * 60 * 5);

        return Jwts.builder()
                .header().keyId(keyId).and()
                .issuer(teamId)
                .issuedAt(now)
                .expiration(expiration)
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }
}