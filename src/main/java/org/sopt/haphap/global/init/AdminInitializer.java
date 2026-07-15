package org.sopt.haphap.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.haphap.domain.admin.entity.Admin;
import org.sopt.haphap.domain.admin.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;

    @Value("${admin.login-id:}")
    private String adminLoginId;

    @Value("${admin.password-hash:}")
    private String adminPasswordHash;

    @Value("${admin.name:}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            log.info("=== admin 계정이 이미 존재하여 AdminInitializer를 건너뜁니다 ===");
            return;
        }
        if (adminLoginId.isBlank() || adminPasswordHash.isBlank()) {
            log.warn("=== ADMIN_LOGIN_ID/ADMIN_PASSWORD_HASH가 설정되지 않아 admin 계정 시딩을 건너뜁니다 ===");
            return;
        }
        adminRepository.save(Admin.create(adminLoginId, adminPasswordHash, adminName));
        log.info("=== 초기 admin 계정을 생성했습니다: {} ===", adminLoginId);
    }
}