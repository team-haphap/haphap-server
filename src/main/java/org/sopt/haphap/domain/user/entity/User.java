package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sopt.haphap.global.common.BaseEntity;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor

public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String anonymousName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private LocalDate birthDate;

    private String gender;

    private String ageRange;

    private String phoneNumber;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    private LocalDateTime withdrawnAt;

    private String appleRefreshToken;

    public void updateAppleRefreshToken(String refreshToken) {
        this.appleRefreshToken = refreshToken;
    }

    public void withdraw() {
        this.name = "탈퇴한 사용자";
        this.email = "withdrawn+" + this.id + "@haphap.local";
        this.phoneNumber = null;
        this.birthDate = null;
        this.gender = null;
        this.ageRange = null;
        this.profileImageUrl = null;
        this.providerId = "WITHDRAWN_" + this.providerId + "_" + System.currentTimeMillis();
        this.appleRefreshToken = null;
        this.withdrawnAt = LocalDateTime.now();
    }
}