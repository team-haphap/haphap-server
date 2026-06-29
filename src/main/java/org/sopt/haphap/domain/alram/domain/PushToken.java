package org.sopt.haphap.domain.alram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.haphap.global.common.BaseEntity;
import org.sopt.haphap.domain.member.domain.User;

@Getter
@Entity
@Table(name = "push_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviceType deviceType;

    @Column(nullable = false)
    private boolean active;

    private PushToken(User user, String fcmToken, DeviceType deviceType) {
        this.user = user;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.active = true;
    }

    public static PushToken create(User user, String fcmToken, DeviceType deviceType) {
        return new PushToken(user, fcmToken, deviceType);
    }

    public void activate(DeviceType deviceType) {
        this.active = true;
        this.deviceType = deviceType;
    }

    public void deactivate() {
        this.active = false;
    }
}