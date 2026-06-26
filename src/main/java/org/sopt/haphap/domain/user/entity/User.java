package org.sopt.haphap.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sopt.haphap.global.common.BaseEntity;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false, unique = true)
    private String providerId;  // 카카오 유저 고유 ID
}