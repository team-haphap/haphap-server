package org.sopt.haphap.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.sopt.haphap.domain.admin.dto.AdminAuthResponse;
import org.sopt.haphap.domain.admin.entity.Admin;
import org.sopt.haphap.domain.admin.repository.AdminRepository;
import org.sopt.haphap.global.code.AdminErrorCode;
import org.sopt.haphap.global.exception.CustomException;
import org.sopt.haphap.global.jwt.JwtProvider;
import org.sopt.haphap.global.jwt.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AdminAuthResponse login(String loginId, String rawPassword) {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(AdminErrorCode.INVALID_ADMIN_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new CustomException(AdminErrorCode.INVALID_ADMIN_CREDENTIALS);
        }

        String accessToken = jwtProvider.createAccessToken(admin.getId(), Role.ADMIN);
        return new AdminAuthResponse(accessToken, admin.getName());
    }
}