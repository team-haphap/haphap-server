package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sopt.haphap.global.common.BaseEntity;

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

    @Column(nullable = false)
    private String anonymousName;

    private String name;

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

    //재로그인-갱신하기
    public void updateProfile(String name, String email, LocalDate birthDate,
                          String gender, String ageRange, String phoneNumber, String profileImageUrl) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (ageRange != null) this.ageRange = ageRange;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl; 
    }
}
